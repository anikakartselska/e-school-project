package com.nevexis.backend.beans


import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InjectionPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.RouterFunctions.resources
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class SPARoutingBean {
    @Bean
    fun client(): RouterFunction<ServerResponse> = resources { request ->
        val path = request.uri().path
        if (path.startsWith("/api") || path.startsWith("/auth")) {
            Mono.empty()
        } else {
            resourceLookup.apply(request)
        }
    }

    private val resourceLookup = RouterFunctions
        .resourceLookupFunction("/**", ClassPathResource("static/"))
        .andThen {
            it.switchIfEmpty(Mono.just(ClassPathResource("static/index.html")))
        }

    @Bean
    @Scope("prototype")
    fun logger(injectionPoint: InjectionPoint): Logger {
        return LoggerFactory.getLogger(
            injectionPoint.methodParameter?.containingClass // constructor
                ?:injectionPoint.field?.declaringClass) // or field injection
    }
}