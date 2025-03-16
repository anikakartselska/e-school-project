package com.nevexis.backend.schoolManagement.messages

import com.nevexis.backend.schoolManagement.BaseService
import mu.KotlinLogging
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import reactor.core.publisher.Sinks
import java.math.BigDecimal

@Service
class MessagesService : BaseService() {

    private val logger = KotlinLogging.logger {}

    @Autowired
    @Qualifier("messages-main-sink")
    private lateinit var sink: Sinks.Many<Message>

    @Autowired
    private lateinit var chatService: ChatService
    fun createMessage(
        message: Message,
        dsl: DSLContext? = null,
    ): Message? =
        runCatching {
            message.let {
                it.copy(id = getMessagesSeqNextVal())
            }.apply {
                chatService.insertMessage(this)
            }
        }.onSuccess {
            logger.info { "HERE" }
            sink.tryEmitNext(it)
        }.onFailure {
            logger.info { "Failed to create action" }
        }.getOrNull()

    fun getMessagesSeqNextVal(): BigDecimal =
        db.select(DSL.field("MESSAGE_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }
}