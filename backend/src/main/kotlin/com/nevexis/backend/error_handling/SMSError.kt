package com.nevexis.backend.error_handling

data class SMSError(val errorType: String, override val message: String) : Exception(message)