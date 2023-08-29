package com.example.camelseda

import org.apache.camel.Exchange
import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.springframework.context.ConfigurableApplicationContext
import java.io.IOException

/**
 * route builder to define our different command types 
 */
class DialCommandRoute(val from: String, val routeId: String) : RouteBuilder() {
  
  @Throws(Exception::class)
  override fun configure() {
    log.debug("###### DialCommandRoute: configure - START")

    // define the route 'dynamically'
    from(from)
      .routeId(routeId)
      .log(LoggingLevel.DEBUG, "Thread being used for: [\${headers.id}] \${body.procName}")
      .process {
        log.debug("###### DialCommandRoute: process (exchange)")
        val message = it.`in`
        val body = message.body
      }.id("[$routeId] prepare command object")
      .log(LoggingLevel.INFO, "DialCommandRoute [\${headers.id}] ")
      .process { it: Exchange ->
        log.debug("###### DialCommandRoute: process (exchange2)")
        it.`in`.body = run(it)
      }

    log.debug("###### DialCommandRoute: configure - DONE")
  }

  /**
   * This typically invokes our logic to run commands. For this example we don't really need it to do anything to 
   * demonstrate the hang except return success all the time.
   */
  @Throws(IOException::class)
  fun run(exchange: Exchange): Response<*> {
    log.debug("###### DialCommandRoute: run - start")

    return try {
      success("OK", exchange.`in`.getHeader("id", String::class.java))
    } catch (e: RuntimeException) {
      log.info("###### DialCommandRoute: run - Exception thrown trying to run dialCommand.call!")
      return failure<Nothing>(failureMessage = e.message ?: e.stackTrace.joinToString(separator = "\\n"),
        id = exchange.`in`.getHeader("id", String::class.java))
    }
  }

}