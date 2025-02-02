package com.nevexis.backend.schoolManagement.file_management

import com.nevexis.backend.schoolManagement.users.UserView
import java.time.LocalDateTime

data class SmsFile(
    val id: Int? = null,
    val fileContent: ByteArray? = null,
    val fileName: String,
    val createdBy: UserView,
    val note: String? = null,
    val createdOn: LocalDateTime? = null
)
