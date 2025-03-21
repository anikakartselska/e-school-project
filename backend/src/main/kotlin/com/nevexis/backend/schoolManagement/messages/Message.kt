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
    val readFromUserIds: List<Int>
)

@Serializable
data class MessageContent(
    val text: String? = null,
    val files: List<FileWithBase64>? = null
)

@Serializable
data class FileWithBase64(
    val name: String,
    val base64: String,
)