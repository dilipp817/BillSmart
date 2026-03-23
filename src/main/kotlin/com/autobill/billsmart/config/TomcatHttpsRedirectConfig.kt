package com.autobill.billsmart.config

import org.apache.catalina.connector.Connector
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.servlet.server.ServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

/**
 * Adds an HTTP connector that redirects to HTTPS when SSL is enabled.
 *
 * Reads `server.ssl.enabled` and `server.port` from the environment so the
 * redirect port matches the configured HTTPS port.
 */
@Configuration
class TomcatHttpsRedirectConfig(
    private val env: Environment
) {

    @Bean
    fun servletContainer(): ServletWebServerFactory {
        val tomcat = TomcatServletWebServerFactory()

        val sslEnabled = env.getProperty("server.ssl.enabled", "false").toBoolean()
        if (sslEnabled) {
            val httpsPort = env.getProperty("server.port", "8443").toInt()
            tomcat.addAdditionalTomcatConnectors(httpConnector(httpsPort))
        }

        return tomcat
    }

    private fun httpConnector(redirectPort: Int): Connector {
        val connector = Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL)
        connector.scheme = "http"
        connector.port = 8081
        connector.secure = false
        connector.redirectPort = redirectPort
        return connector
    }
}
