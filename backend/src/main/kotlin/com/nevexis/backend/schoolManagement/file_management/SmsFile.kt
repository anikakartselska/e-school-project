package com.nevexis.backend.schoolManagement.file_management

import com.nevexis.backend.schoolManagement.users.UserView
import java.time.LocalDateTime

data class SmsFile(
    val id: Int? = null,
    val fileContent: ByteArray,
    val fileName: String,
    val createdBy: UserView,
    val createdOn: LocalDateTime? = null
)
