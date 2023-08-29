package com.example.camelseda

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Splitter
import com.google.common.collect.ImmutableSet
import com.google.common.io.Resources
import jakarta.inject.Inject
import org.apache.camel.Exchange
import org.apache.camel.ExchangePattern
import org.apache.camel.LoggingLevel
import org.apache.camel.RuntimeExchangeException
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.seda.SedaEndpoint
import org.slf4j.MDC
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintStream
import java.util.concurrent.TimeUnit

@Component
class Operator @Inject constructor(val applicationContext: ConfigurableApplicationContext) : RouteBuilder() {
  companion object {
    val DEFAULT_COMMAND_END_POINT = "direct:defaultCommand"
    val PREFIX_DIAL_COMMAND_ROUTE_ID = "dialCommandRoute"
  }
  
  val concurrencyPerUser:Int = 8

  @Throws(Exception::class)
  override fun configure() {
    val shutdownTimeout:Long = 1000L
    val commandTimeout = 0
    val concurrentConsumerCount = 20

    context.isUseMDCLogging = true
    context.shutdownStrategy.timeout = shutdownTimeout
    context.shutdownStrategy.timeUnit = TimeUnit.MILLISECONDS

    log.info(Resources.toString(Resources.getResource("META-INF/MANIFEST.MF"), Charsets.UTF_8))

    val splitter = Splitter.on(' ').omitEmptyStrings()
    val jsonWriter = ObjectMapper().apply {
      setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }.writerFor(Response::class.java)

    val mBeanServer = context.managementStrategy?.managementAgent?.mBeanServer
    if (mBeanServer != null) {
      log.info("Found and registered MBean Server")
      applicationContext.beanFactory.registerSingleton("mBeanServer", mBeanServer)
    } else {
      log.info("MBeanServer not found from context - skipping")
    }

    val idRegex = "--id ([^ ]+)".toRegex()
    
    // Exception handler attempt to ensure RuntimeExchange errors are reported as proper Failure repsonse messages
    onException(RuntimeExchangeException::class.java)
      .log(LoggingLevel.WARN, "Handling RuntimeExchangeException Exception: \${exception.message}")
      .handled(true)
      .onExceptionOccurred {
        it.`in`.body = failure<Unit>(it.exception.message ?: "No message", it.`in`.getHeader("id", String::class.java))
      }
      .transform { body: Response<*> -> jsonWriter.writeValueAsString(body) }

    // define route from servlet to SEDA queue
    from("servlet:dialcommand")
      .routeId("dialCommandServletRoute")
      .convertBodyTo(String::class.java)
      .to("seda:dialcommandqueue?timeout=$commandTimeout")
      .removeHeader("args")

    // define seda queue route with dynamic routing
    from("seda:dialcommandqueue?concurrentConsumers=$concurrentConsumerCount&timeout=$commandTimeout")
      .setExchangePattern(ExchangePattern.InOut)
      .routeId("dialcommandprocess")
      .setHeader("id")
      .body(String::class.java) { it ->
        val id = idRegex.find(it)?.groupValues?.get(1) ?: "unknownId"
        id
      }
      .id("set id header")
      .log(LoggingLevel.INFO, "[\${headers.id}] \${body}")
      .transform { body: String ->
        splitter.splitToList(body).toTypedArray()
      }
      .id("split arguments")
      .setHeader("args", body())
      .id("set arguments header")
      .transformExchange {
          exchange: Exchange -> parse(exchange)
      }
      .dynamicRouter { it: Exchange ->
        dialDynamicRouting(it)
      }
      .transform { body: Response<*> ->
        jsonWriter.writeValueAsString(body)
      }.id("serialize response")
      .log(LoggingLevel.INFO, "[\${headers.id}] \${body}")

//    context.addRoutes(GenericCommandRoute())

  }

  @Synchronized
  fun dialDynamicRouting(exchange: Exchange): String? {
    val body = exchange.`in`.body
    log.info("###### DialDynamicRouting body is: $body")

    // Basically look at body and return next route depending on various conditions
    // this is super hacked version just to demonstrate the hang.
    return when (body) {
      null -> {
        throw RuntimeExchangeException("The exchange contained a null body.", exchange)
      }
      is Response<*> -> {
        null // Once we've received the response, we don't need to keep routing.
      }
      is DIALCommand -> {
        val dialCommand = body as DIALCommand
        val routeId = "$PREFIX_DIAL_COMMAND_ROUTE_ID-${dialCommand.id}"
        
        // we have some commands which kill the route
        if (dialCommand.isKillRoute()) {
          log.info("###### DialDynamicRouting KillRoute - start!")
          log.info("###### DialDynamicRouting KillRoute - routeId: ${routeId}")
          context.routeController.stopRoute(routeId)
          context.removeRoute(routeId)
          exchange.`in`.body = success("OK", exchange.`in`.getHeader("id", String::class.java))
          null
        } else {
          // if not then we do 'normal process' kind of like this..
          log.info("###### DialDynamicRouting Regular Command! RouteId: ${routeId}")

          val route = context.routes.singleOrNull { it.id == routeId }
          if (route != null) {
            log.info("##### DialDynamicRouting Route exists! Would've re-used it!")  
          } else {
            val contextId = dialCommand.id
            log.info("###### DialDynamicRouting DialCommand route does NOT exist! Creating now for contextId: ${contextId}")
            val cr = addNewDialCommandRoute(routeId, contextId, concurrencyPerUser).from
            log.info("####### DialDynamicRouting DialCommand Route added with $cr")
            return cr
          }
          
          // just return success if get here.. this is typically more dynamic
          exchange.`in`.body = success("OK", exchange.`in`.getHeader("id", String::class.java))
          null
        }
      }
      else -> {
        exchange.`in`.body = success("OK", exchange.`in`.getHeader("id", String::class.java))
        null
      }
    }
    }


  @Suppress("UNCHECKED_CAST")
  private fun parse(exchange: Exchange) = parse(exchange, exchange.`in`.body as Array<String>)

  private fun parse(exchange: Exchange, args: Array<String>): DIALCommand? {
    // This is just hard coded to just pull out values for this scenario.
    // Our existing messages are super complext but for this scenario we only care about id and commandName
    
    // array of string is: [ --id, e70c0565, --dhInitialize ] 
    // or: [ --id, e70c0565, --dhKill ]
    // 2nd arg is a context Id and the last one is a command 'type' to run 
    val runnable: DIALCommand? = DIALCommand(args.get(1), args.get(2))
    return runnable
  }

  /**
   * This defines a dynamic route for us to invoke 'commands' at runtime
   */
  private fun addNewDialCommandRoute(routeId: String, contextId: String, concurrency: Int): DialCommandRoute {
    log.info("######## AddNewDialCommandRoute - Adding new dial command route: $routeId")
    val dialCommandRoute = DialCommandRoute("seda:dialcommand:$contextId?concurrentConsumers=$concurrency&timeout=0", routeId)
    context.addRoutes(dialCommandRoute)
    context.getRoute(routeId).properties["contextId"] = contextId

    log.info("######## AddNewDialCommandRoute - Added new dial command contextId: $routeId")
    return dialCommandRoute
  }

}