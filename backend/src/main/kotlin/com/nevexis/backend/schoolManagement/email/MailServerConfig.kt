package com.nevexis.backend.schoolManagement.email

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "notification.mail")
@ConstructorBinding
data class MailServerConfig(
    val host: String,
    val port: Int,
    val sender: String
)