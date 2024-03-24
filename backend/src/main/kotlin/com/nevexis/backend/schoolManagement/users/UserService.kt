package com.nevexis.backend.schoolManagement.users

import com.nevexis.backend.schoolManagement.requests.RequestService
import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.*
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class UserService : UserBaseService() {

    @Autowired
    @Lazy
    private lateinit var requestService: RequestService

    fun updateUser(user: User, loggedUserId: BigDecimal) {
        db.transaction { transaction ->
            transaction.dsl().selectFrom(USER).where(USER.ID.eq(user.id?.toBigDecimal()))
                .fetchOne()
                ?.apply {
                    firstName = user.firstName
                    middleName = user.middleName
                    lastName = user.lastName
                    username = user.username
                    personalNumber = user.personalNumber
                    gender = user.gender.name
                    email = user.email
                    phoneNumber = user.phoneNumber
                    address = user.address
                }?.update()
            requestService.createRequests(user, loggedUserId, transaction.dsl())
        }
    }

    fun findUserWithAllItsRolesById(
        id: BigDecimal,
        schoolId: BigDecimal,
        periodId: BigDecimal,
        username: String
    ) = db.selectFrom(USER).where(USER.ID.eq(id))
        .fetchAnyInto(UserRecord::class.java)?.let { userRecord ->
            val allUserRoles = if (userRecord.username?.equals(username) == true) {
                schoolUserRolesService.getAllUserRoles(userRecord.id!!)
            } else {
                schoolUserRolesService.getAllUserRolesForPeriodAndSchool(id, schoolId, periodId)
            }
            mapUserRecordToUserModel(userRecord, allUserRoles)
        } ?: error("User with id:${id} does not exist in current school and period")

    fun getAllUserViewsBySchool(schoolId: BigDecimal, periodId: BigDecimal, dsl: DSLContext = db): List<UserView> {
        val rolesForSchoolGroupedByUserId = schoolUserRolesService.getAllRolesFromSchoolForPeriod(schoolId, periodId)
        return recordSelectOnConditionStep(dsl).where(
            SCHOOL_USER.SCHOOL_ID.eq(schoolId).and(
                SCHOOL_USER_PERIOD.STATUS.eq(RequestStatus.APPROVED.name).and(
                    SCHOOL_USER_PERIOD.PERIOD_ID.eq(periodId)
                )
            )
        ).map {
            val userRecord = it.into(UserRecord::class.java)
            mapToUserView(userRecord, rolesForSchoolGroupedByUserId[userRecord.id] ?: emptyList())
        }
    }


    fun getUserByIdWithoutRoles(userId: BigDecimal, dsl: DSLContext): User? {
        return dsl.selectFrom(USER).where(USER.ID.eq(userId)).fetchAny()?.map {
            mapUserRecordToUserModel(it.into(UserRecord::class.java), emptyList())
        }
    }


    fun getAllStudentsInSchoolClass(
        schoolClassId: BigDecimal,
        periodId: BigDecimal
    ): List<StudentView> =
        studentRecordSelectOnConditionStep()
            .where(
                STUDENT_SCHOOL_CLASS.SCHOOL_CLASS_ID.eq(schoolClassId).and(
                    SCHOOL_USER_PERIOD.PERIOD_ID.eq(periodId)
                        .and(SCHOOL_USER_PERIOD.STATUS.eq(RequestStatus.APPROVED.name))
                )
            ).orderBy(STUDENT_SCHOOL_CLASS_PERIOD.NUMBER_IN_CLASS)
            .fetch()
            .map { record ->
                val numberInClass = record.get(DSL.field("NUMBER_IN_CLASS", BigDecimal::class.java))?.toInt()
                record.into(UserRecord::class.java).let { userRecord ->
                    StudentView(
                        id = userRecord.id!!,
                        email = userRecord.email!!,
                        firstName = userRecord.firstName!!,
                        middleName = userRecord.middleName!!,
                        lastName = userRecord.lastName!!,
                        username = userRecord.username!!,
                        numberInClass = numberInClass
                    )
                }
            }


    fun findUsersByTheirSchoolRolePeriodIds(
        schoolRolePeriodIds: List<BigDecimal>,
        dsl: DSLContext = db
    ): Map<BigDecimal, OneRoleUser> = recordSelectOnConditionStepJoinedWithUserRoles(dsl)
        .where(SCHOOL_ROLE_PERIOD.ID.`in`(schoolRolePeriodIds))
        .fetch().associate {
            val schoolUserRole = schoolUserRolesService.mapToModel(it)
            it.get(SCHOOL_ROLE_PERIOD.ID)!! to
                    mapUserRecordToOneRoleModel(it.into(UserRecord::class.java), schoolUserRole)
        }

    fun findUsersByTheirSchoolUserPeriodIds(
        schoolUserPeriodIds: List<BigDecimal>,
        dsl: DSLContext
    ): Map<BigDecimal, User> = recordSelectOnConditionStepJoinedWithUserRoles(dsl)
        .where(SCHOOL_USER_PERIOD.ID.`in`(schoolUserPeriodIds))
        .fetch().associate {
            it.get(SCHOOL_USER_PERIOD.ID)!! to
                    mapUserRecordToUserModel(it.into(UserRecord::class.java), emptyList())
        }

    fun findUsersByItsRoleIdsAndPeriodId(
        roleIds: List<BigDecimal>,
        periodId: BigDecimal,
        dsl: DSLContext = db
    ): List<OneRoleUser> = recordSelectOnConditionStepJoinedWithUserRoles(dsl)
        .where(SCHOOL_USER_ROLE.ID.`in`(roleIds))
        .and(SCHOOL_ROLE_PERIOD.PERIOD_ID.eq(periodId))
        .and(SCHOOL_ROLE_PERIOD.STATUS.eq(RequestStatus.APPROVED.name))
        .and(SCHOOL_USER_PERIOD.PERIOD_ID.eq(periodId))
        .and(SCHOOL_USER_PERIOD.STATUS.eq(RequestStatus.APPROVED.name))
        .fetch().map {
            val schoolUserRole = schoolUserRolesService.mapToModel(it)
            mapUserRecordToOneRoleModel(it.into(UserRecord::class.java), schoolUserRole)
        }

    fun findUserByItsRoleIdAndPeriodId(
        roleId: BigDecimal,
        periodId: BigDecimal,
        dsl: DSLContext = db
    ) = recordSelectOnConditionStepJoinedWithUserRoles(dsl)
        .where(SCHOOL_USER_ROLE.ID.eq(roleId))
        .and(SCHOOL_ROLE_PERIOD.PERIOD_ID.eq(periodId))
        .and(SCHOOL_ROLE_PERIOD.STATUS.eq(RequestStatus.APPROVED.name))
        .and(SCHOOL_USER_PERIOD.PERIOD_ID.eq(periodId))
        .and(SCHOOL_USER_PERIOD.STATUS.eq(RequestStatus.APPROVED.name))
        .fetchAny()?.let {
            val schoolUserRole = schoolUserRolesService.mapToModel(it)
            mapUserRecordToOneRoleModel(it.into(UserRecord::class.java), schoolUserRole)
        } ?: error("User with role id $roleId does not exist in period id $periodId")


}