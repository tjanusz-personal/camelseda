package com.example.camelseda

import org.springframework.boot.runApplication
import org.springframework.web.context.ServletContextAware
import jakarta.servlet.ServletContext
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.WebApplicationType;
import org.apache.camel.spring.boot.CamelSpringBootApplicationController

@SpringBootApplication
class CamelsedaApplication : ServletContextAware {
	private lateinit var _servletContext: ServletContext

	override fun setServletContext(servletContext: ServletContext) {
		_servletContext = servletContext
	}

	private val _springApplication: SpringApplication by lazy {
		SpringApplicationBuilder(CamelsedaApplication::class.java, Operator::class.java, OperatorConfig::class.java)
			.web(WebApplicationType.SERVLET)
			.bannerMode(Banner.Mode.OFF)
			.build()
	}

	fun run(args: Array<String>) {
		System.setProperty("camel.springboot.main-run-controller", "true")
		System.setProperty("camel.springboot.name", "DIAL-Operator")
		System.setProperty("server.session.timeout", "0")
		System.setProperty("management.security.enabled", "false")
		System.setProperty("camel.health.check.indicator.enabled", "false")
		System.setProperty("management.endpoints.web.base-path", "/manage")
		System.setProperty("management.endpoint.health.show-details", "always")
		System.setProperty("management.endpoints.web.exposure.include", "*")

		_springApplication.run(*args)
			.getBean(CamelSpringBootApplicationController::class.java)
			.run()
	}
	
	companion object {
		@JvmStatic
		fun main(args: Array<String>) {
				CamelsedaApplication().run(args)
		}

	}
}