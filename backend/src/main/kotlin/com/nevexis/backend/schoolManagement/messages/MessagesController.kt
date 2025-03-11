package com.nevexis.backend.schoolManagement.messages

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api/messages")
class MessagesController {
    @Autowired
    @Qualifier("messages-main-sink")
    private lateinit var sink: Sinks.Many<Message>

    @Autowired
    private lateinit var messagesService: MessagesService

    @RequestMapping(path = ["/subscribe-actions"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun sse(): Flux<ServerSentEvent<Message>> = sink.asFlux().map { e: Message ->
        ServerSentEvent.builder(e).id(e.id.toString()).build()
    }
}