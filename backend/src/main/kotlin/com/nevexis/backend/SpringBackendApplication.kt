package com.nevexis.backend

import com.nevexis.backend.schoolManagement.email.MailServerConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(MailServerConfig::class)
@SpringBootApplication(exclude = [R2dbcAutoConfiguration::class])
class SpringBackendApplication

fun main(args: Array<String>) {
    runApplication<SpringBackendApplication>(*args)
}
