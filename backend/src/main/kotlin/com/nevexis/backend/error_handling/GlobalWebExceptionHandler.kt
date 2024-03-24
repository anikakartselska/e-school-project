package de.akabank.rvmbackend.error_handling

import mu.KotlinLogging
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono


@Component
@Order(-2)
class GlobalErrorWebExceptionHandler(
    errorAttributes: ErrorAttributes?,
    serverCodecConfigurer: ServerCodecConfigurer,
    applicationContext: ApplicationContext?
) : AbstractErrorWebExceptionHandler(
    errorAttributes,
    WebProperties.Resources(),
    applicationContext
) {
    init {
        this.setMessageWriters(serverCodecConfigurer.writers)
    }

    private val logger = KotlinLogging.logger {}

    override fun getRoutingFunction(errorAttributes: ErrorAttributes?): RouterFunction<ServerResponse> =
        RouterFunctions.route(RequestPredicates.all()) { request: ServerRequest ->
            renderErrorResponse(request)
        }

    fun renderErrorResponse(request: ServerRequest): Mono<ServerResponse?> = getErrorAttributes(
        request,
        ErrorAttributeOptions.defaults().including(
            ErrorAttributeOptions.Include.EXCEPTION,
            ErrorAttributeOptions.Include.STACK_TRACE,
            ErrorAttributeOptions.Include.MESSAGE
        )
    ).let {
        ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(it))
    }
}
