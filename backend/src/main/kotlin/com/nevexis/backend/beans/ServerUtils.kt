package com.nevexis.backend.beans

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext
import org.springframework.stereotype.Component
import java.net.InetAddress

@Component
class ServerUtils(@Value("\${security.require-ssl}") private val httpOverSsl: Boolean) {
    @Autowired
    private lateinit var server: ReactiveWebServerApplicationContext

    fun getPort() = server.webServer.port

    fun getHostname(): String = InetAddress.getLocalHost().hostAddress

    fun getServerUrlWithoutProtocol() = "${if (httpOverSsl) "https:" else "http:"}//${getHostname()}:${getPort()}"
}