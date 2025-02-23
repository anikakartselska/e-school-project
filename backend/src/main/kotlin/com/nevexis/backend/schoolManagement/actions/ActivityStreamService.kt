package com.nevexis.backend.schoolManagement.actions

import com.nevexis.backend.schoolManagement.users.UserService
import mu.KotlinLogging
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import reactor.core.publisher.Sinks
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class ActivityStreamService {
    private val logger = KotlinLogging.logger {}

    @Autowired
    @Qualifier("activity-stream-main-sink")
    private lateinit var sink: Sinks.Many<Actions>

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var actionsModelService: ActionsService

    fun createAction(
        periodId: BigDecimal,
        schoolId: BigDecimal,
        userId: BigDecimal,
        forUserIds: List<BigDecimal>,
        dsl: DSLContext? = null,
        action: String,
    ): Actions? =
        runCatching {
            Actions(
                id = actionsModelService.getActionsSeqNextVal().toInt(),
                executedTime = LocalDateTime.now(),
                executedBy = userService.getUserViewsById(userId, schoolId, periodId),
                periodId = periodId,
                schoolId = schoolId,
                action = action
            )
        }.onSuccess {
            logger.info { "HERE" }
            actionsModelService.insertActions(it, forUserIds, dsl)
            sink.tryEmitNext(it)
        }.onFailure {
            logger.info { "Failed to create action" }
        }.getOrNull()
}