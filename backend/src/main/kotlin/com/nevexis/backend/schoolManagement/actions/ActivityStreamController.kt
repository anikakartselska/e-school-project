package com.nevexis.backend.schoolManagement.actions

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.security.Principal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api/stream")
class ActivityStreamController {
    @Autowired
    @Qualifier("activity-stream-main-sink")
    private lateinit var sink: Sinks.Many<Actions>

    @Autowired
    private lateinit var actionsService: ActionsService

    @PostMapping("/get-actions-with-filters-and-pagination")
    fun getActionsWithFiltersAndPagination(
        @RequestBody actionsFetchingInformationDTO: ActionsFetchingInformationDTO,
    ) = actionsService.fetchActionsWithFiltersAndPagination(actionsFetchingInformationDTO)

    @GetMapping("/get-last-five-actions-for-user")
    fun getLastFiveActionsOnlyGorUser(principal: Principal) =
        actionsService.fetchLastFiveActionsForUser(principal.name.toBigDecimal())

    @RequestMapping(path = ["/subscribe-actions"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun sse(): Flux<ServerSentEvent<Actions>> = sink.asFlux().map { e: Actions ->
        ServerSentEvent.builder(e).id(e.id.toString()).build()
    }
}