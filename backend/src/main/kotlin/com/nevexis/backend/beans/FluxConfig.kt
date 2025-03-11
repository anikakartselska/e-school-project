package com.nevexis.backend.beans

import com.nevexis.backend.schoolManagement.actions.Actions
import com.nevexis.backend.schoolManagement.messages.Message
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.WebFluxConfigurer
import reactor.core.publisher.Sinks
import reactor.util.concurrent.Queues

@Configuration
class FluxConfig : WebFluxConfigurer {

    @Bean
    @Qualifier("activity-stream-main-sink")
    fun getSink() =
        Sinks.many().multicast().onBackpressureBuffer<Actions>(Queues.XS_BUFFER_SIZE, false)

    @Bean
    @Qualifier("messages-main-sink")
    fun getMessagesSink() =
        Sinks.many().multicast().onBackpressureBuffer<Message>(Queues.XS_BUFFER_SIZE, false)


}