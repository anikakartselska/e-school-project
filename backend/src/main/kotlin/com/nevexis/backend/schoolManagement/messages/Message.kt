package com.nevexis.backend.schoolManagement.messages

import com.nevexis.backend.schoolManagement.users.UserView
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

data class Message(
    val id: BigDecimal? = null,
    val user: UserView,
    val content: MessageContent,
    val sendOn: LocalDateTime,
    val chatId: BigDecimal,
    val read: Boolean = false
)

@Serializable
data class MessageContent(
    val text: String? = null,
    val picture: String? = null
)