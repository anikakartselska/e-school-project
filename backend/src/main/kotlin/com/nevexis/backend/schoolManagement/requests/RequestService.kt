package com.nevexis.backend.schoolManagement.requests

import com.nevexis.backend.nullIfPrimaryKeyIsNull
import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.users.User
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.backend.schoolManagement.users.roles.SchoolRolesService
import com.nevexis.backend.schoolManagement.users.school_user.SchoolUserService
import com.nevexis.backend.schoolManagement.users.user_security.UserSecurityService
import com.nevexis.`demo-project`.jooq.tables.records.RequestRecord
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.REQUEST
import com.nevexis.`demo-project`.jooq.tables.references.USER
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.decodeFromString
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.Result
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.reflect.KClass

@Service
class RequestService : BaseService() {
    @Autowired
    private lateinit var userSecurityService: UserSecurityService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var schoolRolesService: SchoolRolesService

    @Autowired
    private lateinit var schoolUserService: SchoolUserService

    companion object {
        private val RESOLVED_BY_USER_ALIAS = USER.`as`("ResolvedByUser")
    }

    fun fetchUserRequestsForSchoolAndPeriod(
        schoolId: BigDecimal,
        periodId: BigDecimal,
        userId: BigDecimal? = null
    ): List<Request> {
        val registrationRequests =
            recordSelectOnConditionStep()
                .where(REQUEST.SCHOOL_ID.eq(schoolId).and(REQUEST.PERIOD_ID.eq(periodId)))
                .and(REQUEST.REQUEST_VALUE.like("%schoolUserPeriodId%"))
                .apply {
                    if (userId != null) {
                        and(REQUEST.REQUESTED_BY_USER_ID.eq(userId))
                    }
                }
                .orderBy(REQUEST.REQUEST_DATE.desc())
                .fetch()
        val requestType = RequestValueJson.UserRegistration::class
        val requestValuesIdsToValue = mapRequestValues(
            registrationRequests.map { it.into(RequestRecord::class.java) },
            requestType
        )

        return mapRecordToRequestsModel(
            registrationRequests,
            requestValuesIdsToValue,
            RequestValueJson.UserRegistration::class
        )
    }

    fun changeRequestStatus(requestIds: List<BigDecimal>, requestStatus: RequestStatus, resolvedByUserId: BigDecimal) {
        db.transaction { transaction ->
            val requests = transaction.dsl().selectFrom(REQUEST)
                .where(REQUEST.ID.`in`(requestIds))
                .fetch()

            requests.map { request ->
                when (val decodedRequestValue = decodeRequestValueJson(request?.requestValue!!)) {
                    is RequestValueJson.Role -> schoolRolesService.changeSchoolUserRolePeriodStatus(
                        decodedRequestValue,
                        requestStatus,
                        transaction.dsl()
                    )

                    is RequestValueJson.UserRegistration -> schoolUserService.changeSchoolUserPeriodStatus(
                        decodedRequestValue,
                        requestStatus,
                        transaction.dsl()
                    )
                }

                request.apply {
                    this.requestStatus = requestStatus.name
                    this.resolvedDate = LocalDateTime.now()
                    this.resolvedByUserId = resolvedByUserId
                }.update()
            }
        }
    }

    fun fetchRoleRequestsForSchoolAndPeriod(
        schoolId: BigDecimal,
        periodId: BigDecimal
    ): List<Request> {
        val registrationRequests =
            recordSelectOnConditionStep()
                .where(REQUEST.SCHOOL_ID.eq(schoolId).and(REQUEST.PERIOD_ID.eq(periodId)))
                .and(REQUEST.REQUEST_VALUE.like("%schoolUserRolePeriodId%"))
                .orderBy(REQUEST.REQUEST_DATE.desc())
                .fetch()
        val requestType = RequestValueJson.Role::class
        val requestValuesIdsToValue = mapRequestValues(
            registrationRequests.map { it.into(RequestRecord::class.java) },
            requestType
        )

        return mapRecordToRequestsModel(
            registrationRequests,
            requestValuesIdsToValue,
            RequestValueJson.Role::class
        )

    }

    private fun mapRecordToRequestsModel(
        registrationRequests: Result<Record>,
        requestValuesIdsToValue: Map<BigDecimal, RequestValue>,
        type: KClass<out RequestValueJson>
    ): MutableList<Request> =
        registrationRequests.map { record ->
            record.into(RequestRecord::class.java).let {
                val requestValueJson = decodeRequestValueJson(it.requestValue!!)
                Request(
                    id = it.id!!.toInt(),
                    requestedByUser = userService.mapToUserView(record.into(UserRecord::class.java), emptyList()),
                    requestValue = when (type) {
                        RequestValueJson.UserRegistration::class -> requestValuesIdsToValue[(requestValueJson as RequestValueJson.UserRegistration).schoolUserPeriodId.toBigDecimal()]!!
                        RequestValueJson.Role::class -> requestValuesIdsToValue[(requestValueJson as RequestValueJson.Role).schoolUserRolePeriodId.toBigDecimal()]!!
                        else -> {
                            error("Unknown Registration Type")
                        }
                    },
                    requestDate = it.requestDate!!,
                    requestStatus = RequestStatus.valueOf(it.requestStatus!!),
                    resolvedByUser = record.into(RESOLVED_BY_USER_ALIAS).nullIfPrimaryKeyIsNull()
                        ?.into(UserRecord::class.java)
                        ?.let { userRecord -> userService.mapToUserView(userRecord, emptyList()) },
                    resolvedDate = it.resolvedDate
                )
            }
        }

    private fun decodeRequestValueJson(it: String) = decodeFromString(
        RequestValueJson.serializer(),
        it
    )

    private fun recordSelectOnConditionStep() = db.select(
        REQUEST.asterisk(),
        USER.asterisk(),
        RESOLVED_BY_USER_ALIAS.asterisk()
    ).from(REQUEST)
        .leftJoin(USER).on(USER.ID.eq(REQUEST.REQUESTED_BY_USER_ID))
        .leftJoin(RESOLVED_BY_USER_ALIAS).on(RESOLVED_BY_USER_ALIAS.ID.eq(REQUEST.RESOLVED_BY_USER_ID))

    private fun mapRequestValues(
        requestRecords: List<RequestRecord>,
        type: KClass<out RequestValueJson>
    ): Map<BigDecimal, RequestValue> {
        val requestValues = requestRecords.map {
            decodeFromString(
                RequestValueJson.serializer(),
                it.requestValue!!
            )
        }
        return when (type) {
            RequestValueJson.Role::class -> {
                userService.findUsersByTheirSchoolRolePeriodIds(
                    requestValues.map { (it as RequestValueJson.Role).schoolUserRolePeriodId.toBigDecimal() },
                    db
                ).mapValues { (_, oneRoleUser) -> RequestValue.Role(oneRoleUser) }
            }

            RequestValueJson.UserRegistration::class -> {
                userService.findUsersByTheirSchoolUserPeriodIds(
                    requestValues.map { (it as RequestValueJson.UserRegistration).schoolUserPeriodId.toBigDecimal() },
                    db
                ).mapValues { (_, user) -> RequestValue.UserRegistration(user) }
            }

            else -> {
                error("Not existing ResultValueJson type")
            }

        }
    }

    fun createRequests(user: User, loggedUserId: BigDecimal? = null, dsl: DSLContext = db) {
        dsl.transaction { transaction ->
            val userId = user.id?.toBigDecimal() ?: userSecurityService.createUser(user, transaction.dsl())
            schoolUserService.createSchoolUsersFromListOfSchoolUserRoles(
                userId,
                user.roles ?: emptyList(),
                transaction.dsl()
            ).map {
                RequestRecord(
                    id = getRequestSeqNextVal(),
                    requestStatus = RequestStatus.PENDING.name,
                    periodId = it.periodId,
                    schoolId = it.schoolId,
                    requestedByUserId = loggedUserId ?: userId,
                    requestValue = Json.encodeToString(RequestValueJson.UserRegistration(it.valueId.toInt())),
                    requestDate = LocalDateTime.now(),

                    )
            }.also {
                transaction.dsl().batchInsert(it).execute()
            }

            schoolRolesService.createSchoolUserRoles(
                userId,
                user.roles ?: emptyList(),
                transaction.dsl()
            ).map {
                RequestRecord(
                    id = getRequestSeqNextVal(),
                    requestStatus = RequestStatus.PENDING.name,
                    periodId = it.periodId,
                    schoolId = it.schoolId,
                    requestedByUserId = loggedUserId ?: userId,
                    requestValue = Json.encodeToString(RequestValueJson.Role(it.valueId.toInt())),
                    requestDate = LocalDateTime.now()
                )
            }.also {
                transaction.dsl().batchInsert(it).execute()
            }
        }
    }


    fun getRequestSeqNextVal(): BigDecimal =
        db.select(DSL.field("REQUEST_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

}