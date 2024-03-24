package com.nevexis.backend.error_handling

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

val HTTP_BUSINESS_ERROR_STATUS = HttpStatus.I_AM_A_TEAPOT // 418

@ControllerAdvice
class GlobalRvmErrorHandler {
    @ExceptionHandler(SMSError::class)
    fun handleBusinessException(ex: SMSError): ResponseEntity<out Any> {
        return ResponseEntity.status(HTTP_BUSINESS_ERROR_STATUS)
            .body(ex)
    }

}
