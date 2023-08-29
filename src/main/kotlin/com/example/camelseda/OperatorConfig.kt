package com.example.camelseda

import org.apache.camel.component.servlet.CamelHttpTransportServlet
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration class OperatorConfig {
  @Bean fun servletRegistrationBean() = ServletRegistrationBean(CamelHttpTransportServlet(), "/camel/*")
    .apply {
      setName("CamelServlet")
    }
}