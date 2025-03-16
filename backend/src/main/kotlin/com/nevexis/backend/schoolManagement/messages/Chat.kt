package com.nevexis.backend.schoolManagement.messages

import com.nevexis.backend.schoolManagement.users.UserView
import java.math.BigDecimal

data class Chat(
    val id: BigDecimal? = null,
    val chatName: String,
    val chatType: ChatType,
    val chatMembers: List<UserView>? = null
)

enum class ChatType {
    DIRECT_MESSAGES, GROUP_CHAT
}