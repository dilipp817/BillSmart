package com.autobill.billsmart.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * Binds the `app.*` keys from application.properties so that the IDE
 * recognises them and auto-completes them correctly.
 */
@Component
@ConfigurationProperties(prefix = "app")
data class AppProperties(
    var name: String = "BillSmart",
    var version: String = "1.0.0",
    var description: String = "",
    var jwt: JwtProperties = JwtProperties()
) {
    data class JwtProperties(
        var secret: String = "",
        var expiration: Long = 86400000L
    )
}

