package com.nevexis.backend.schoolManagement.requests

import com.nevexis.backend.error_handling.SMSError
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
        val requestIdToRequestValue = mapRequestValues(
            registrationRequests.map { it.into(RequestRecord::class.java) },
            requestType
        )

        return mapRecordToRequestsModel(
            registrationRequests,
            requestIdToRequestValue,
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
                    is RequestValueJson.Role -> {
                        if (decodedRequestValue.status == null || requestStatus != RequestStatus.REJECTED) {
                            schoolRolesService.changeSchoolUserRolePeriodStatus(
                                decodedRequestValue,
                                decodedRequestValue.status ?: requestStatus,
                                transaction.dsl()
                            )
                        }
                    }

                    is RequestValueJson.UserRegistration -> {
                        if (decodedRequestValue.status == null || requestStatus != RequestStatus.REJECTED) {
                            schoolUserService.changeSchoolUserPeriodStatus(
                                decodedRequestValue,
                                decodedRequestValue.status ?: requestStatus,
                                transaction.dsl()
                            )
                        }
                    }
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
        val requestIdToRequestValue = mapRequestValues(
            registrationRequests.map { it.into(RequestRecord::class.java) },
            requestType
        )

        return mapRecordToRequestsModel(
            registrationRequests,
            requestIdToRequestValue,
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
                Request(
                    id = it.id!!.toInt(),
                    requestedByUser = userService.mapToUserView(record.into(UserRecord::class.java), emptyList()),
                    requestValue = when (type) {
                        RequestValueJson.UserRegistration::class -> requestValuesIdsToValue[it.id!!]!!
                        RequestValueJson.Role::class -> requestValuesIdsToValue[it.id!!]!!
                        else -> {
                            error("Unknown Request Type")
                        }
                    },
                    requestDate = it.requestDate!!,
                    requestStatus = RequestStatus.valueOf(it.requestStatus!!),
                    resolvedByUser = record.into(RESOLVED_BY_USER_ALIAS).nullIfPrimaryKeyIsNull()
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
        val requestIdToRequestValue = requestRecords.associate {
            it.id!! to decodeFromString(
                RequestValueJson.serializer(),
                it.requestValue!!
            )
        }
        return when (type) {
            RequestValueJson.Role::class -> {
                userService.findUsersByTheirSchoolRolePeriodIds(
                    requestIdToRequestValue.mapValues { (_, requestValueJson) -> (requestValueJson as RequestValueJson.Role).schoolUserRolePeriodId.toBigDecimal() to requestValueJson.status },
                    db
                ).mapValues { (_, oneRoleUserToRequestStatus) ->
                    RequestValue.Role(
                        oneRoleUserToRequestStatus.first,
                        oneRoleUserToRequestStatus.second
                    )
                }
            }

            RequestValueJson.UserRegistration::class -> {
                userService.findUsersByTheirSchoolUserPeriodIds(
                    requestIdToRequestValue.mapValues { (_, requestValueJson) -> (requestValueJson as RequestValueJson.UserRegistration).schoolUserPeriodId.toBigDecimal() to requestValueJson.status },
                    db
                ).mapValues { (_, requestStatusToUserPair) ->
                    RequestValue.UserRegistration(
                        requestStatusToUserPair.first,
                        requestStatusToUserPair.second
                    )
                }
            }

            else -> {
                error("Not existing ResultValueJson type")
            }
        }
    }

    fun createRequests(users: List<User>, loggedUserId: BigDecimal? = null, dsl: DSLContext = db): Set<BigDecimal> {
        return dsl.transactionResult { transaction ->
            users.associate { user ->
                val userId = user.id?.toBigDecimal() ?: userSecurityService.createUser(user, transaction.dsl())
                val registrationRequests = schoolUserService.createSchoolUsersFromListOfSchoolUserRoles(
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
                }

                val roleRequests = schoolRolesService.createSchoolUserRoles(
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
                }

                userId to registrationRequests + roleRequests
            }.also { userIdToRequests ->
                transaction.dsl().batchInsert(userIdToRequests.values.flatten()).execute()
            }.keys
        }
    }

    fun createUserChangeStatusRequest(
        userId: BigDecimal,
        newStatus: RequestStatus,
        periodId: BigDecimal,
        schoolId: BigDecimal,
        loggedUserId: BigDecimal
    ) {
        db.transaction { transaction ->
            val schoolUserPeriodId = schoolUserService.fetchSchoolUserPeriodRecordByUserIdSchoolAndPeriod(
                userId,
                schoolId,
                periodId,
                transaction.dsl()
            )?.id!!
            transaction.dsl().selectFrom(REQUEST).where(REQUEST.REQUEST_STATUS.eq(RequestStatus.PENDING.name))
                .and(
                    REQUEST.REQUEST_VALUE.like("%\"schoolUserPeriodId\":${schoolUserPeriodId}%")
                        .and(REQUEST.REQUEST_VALUE.like("%\"status\":\"${newStatus}\"%"))
                ).fetchAny()?.also {
                    throw SMSError(
                        "Вече съществуващи данни",
                        "Вече има създадена заявка за промяна на статуса на текущия потребител.Проверете заявките."
                    )
                }
            transaction.dsl().newRecord(REQUEST).apply {
                id = getRequestSeqNextVal()
                requestStatus = RequestStatus.PENDING.name
                this.periodId = periodId
                this.schoolId = schoolId
                requestedByUserId = loggedUserId
                requestValue =
                    Json.encodeToString(
                        RequestValueJson.UserRegistration(
                            schoolUserPeriodId.toInt(),
                            newStatus
                        )
                    )
                requestDate = LocalDateTime.now()

            }.insert()
        }
    }

    fun createRoleChangeStatusRequest(
        roleId: BigDecimal,
        newStatus: RequestStatus,
        periodId: BigDecimal,
        schoolId: BigDecimal,
        loggedUserId: BigDecimal
    ) {
        db.transaction { transaction ->
            val schoolUserRolePeriodId =
                schoolRolesService.getSchoolRolePeriodRecordByRoleId(roleId, periodId, transaction.dsl())?.id!!

            transaction.dsl().selectFrom(REQUEST).where(REQUEST.REQUEST_STATUS.eq(RequestStatus.PENDING.name))
                .and(
                    REQUEST.REQUEST_VALUE.like("%\"schoolUserRolePeriodId\":${schoolUserRolePeriodId}%")
                        .and(REQUEST.REQUEST_VALUE.like("%\"status\":\"${newStatus}\"%"))
                ).fetchAny()?.also {
                    throw SMSError(
                        "Вече съществуващи данни",
                        "Вече има създадена заявка за промяна на статуса на избраната роля.Проверете заявките."
                    )
                }
            transaction.dsl().newRecord(REQUEST).apply {
                id = getRequestSeqNextVal()
                requestStatus = RequestStatus.PENDING.name
                this.periodId = periodId
                this.schoolId = schoolId
                requestedByUserId = loggedUserId
                requestValue =
                    Json.encodeToString(
                        RequestValueJson.Role(
                            schoolUserRolePeriodId.toInt(),
                            newStatus
                        )
                    )
                requestDate = LocalDateTime.now()

            }.insert()
        }
    }

    fun getRequestSeqNextVal(): BigDecimal =
        db.select(DSL.field("REQUEST_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

}