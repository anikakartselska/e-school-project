package com.nevexis.backend.schoolManagement.actions

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.`demo-project`.jooq.tables.records.ActionsRecord
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.ACTIONS
import com.nevexis.`demo-project`.jooq.tables.references.ACTION_USER
import com.nevexis.`demo-project`.jooq.tables.references.USER
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class ActionsService : BaseService() {

    @Autowired
    private lateinit var userService: UserService

    fun fetchActionsWithFiltersAndPagination(
        actionsFetchingInformationDTO: ActionsFetchingInformationDTO,
    ): List<Actions> {
        val (startRange,
            endRange,
            forUserId,
            periodId,
            schoolId) = actionsFetchingInformationDTO
        val ROWNUM = DSL.field("ROWNUM", Int::class.java)
        val RN = DSL.field("RN", Int::class.java)
        val EXECUTED_TIME = DSL.field("EXECUTED_TIME", LocalDateTime::class.java)
        val EXECUTED_BY = DSL.field("EXECUTED_BY", BigDecimal::class.java)
        val ACTIONS_TABLE = DSL.table("ACTIONS")

        val selectActionsWithAppliedFiltersQuery = db.select(ACTIONS.asterisk(), ACTION_USER.asterisk())
            .from(ACTIONS)
            .leftJoin(ACTION_USER)
            .on(ACTIONS.ID.eq(ACTION_USER.ACTION_ID))
            .apply {
                var condition = DSL.noCondition()
                var actionsCondition = DSL.noCondition()
                //period filter
                condition = condition.and(ACTIONS.PERIOD_ID.eq(periodId))
                condition = condition.and(ACTIONS.SCHOOL_ID.eq(schoolId))

                //executedBy filter
                condition = condition.and(ACTION_USER.USER_ID.eq(forUserId))

                this.where(condition.and(actionsCondition))
            }
            .orderBy(ACTIONS.EXECUTED_TIME.desc()).asTable(ACTIONS_TABLE)

        // The following is done for fetching with range in oracle 11 with jooq due to lack of limit/offset functions.
        // for reference: https://www.jooq.org/doc/latest/manual/sql-building/sql-statements/select-statement/limit-clause
        val selectActionsRownumWithEndRangeFilterQuery =
            db.select(selectActionsWithAppliedFiltersQuery.asterisk(), ROWNUM.`as`(RN)).from(
                selectActionsWithAppliedFiltersQuery
            ).where(ROWNUM.lessOrEqual(endRange)).asTable(ACTIONS)

        return db.select(selectActionsRownumWithEndRangeFilterQuery.asterisk(), USER.asterisk()).from(
            selectActionsRownumWithEndRangeFilterQuery
        ).leftJoin(USER)
            .on(EXECUTED_BY.eq(USER.ID))
            .where(RN.greaterThan(startRange))
            .orderBy(EXECUTED_TIME.desc())
            .fetch().map { mapRecordToInternalModel(it) }

    }

    fun fetchLastFiveActionsForUser(userId: BigDecimal): List<Actions> = with(ACTIONS) {
        getActionsSelectConditionStep()
            .where(ACTION_USER.USER_ID.eq(userId))
            .orderBy(this.EXECUTED_TIME.desc())
            .maxRows(5)
            .fetch()
            .map { mapRecordToInternalModel(it) }
    }

    fun insertActions(action: Actions, forUserIds: List<BigDecimal>, dsl: DSLContext? = db) {
        (dsl ?: db).newRecord(ACTIONS).apply {
            id = action.id.toBigDecimal()
            executedTime = action.executedTime
            executedBy = action.executedBy.id.toBigDecimal()
            periodId = action.periodId
            schoolId = action.schoolId
            this.action = action.action
        }.also {
            it.insert()
        }
        forUserIds.distinct().map {
            (dsl ?: db).newRecord(ACTION_USER)
                .apply {
                    actionId = action.id.toBigDecimal()
                    userId = it
                }
        }.also {
            (dsl ?: db).batchInsert(it).execute()
        }
    }

    private fun mapRecordToInternalModel(record: Record): Actions = with(record.into(ActionsRecord::class.java)) {
        Actions(
            id = this.id?.toInt()!!,
            executedTime = this.executedTime!!,
            executedBy = userService.mapToUserView(record.into(UserRecord::class.java), emptyList()),
            periodId = this.periodId!!,
            schoolId = this.schoolId!!,
            action = this.action!!
        )
    }

    private fun com.nevexis.`demo-project`.jooq.tables.Actions.getActionsSelectConditionStep() = db.select(
        this.asterisk(),
        USER.asterisk(),
        ACTION_USER.asterisk()
    ).from(this)
        .leftJoin(USER).on(this.EXECUTED_BY.eq(USER.ID))
        .leftJoin(ACTION_USER).on(ACTION_USER.ACTION_ID.eq(this.ID))

    fun getActionsSeqNextVal(): BigDecimal =
        db.select(DSL.field("ACTIONS_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

}