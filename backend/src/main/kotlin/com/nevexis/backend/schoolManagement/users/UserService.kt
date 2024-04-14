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
            requestService.createRequests(users = listOf(user), loggedUserId, transaction.dsl())
        }
    }

    fun findUserWithAllItsRolesById(
        id: BigDecimal,
        schoolId: BigDecimal,
        periodId: BigDecimal,
        principalName: String
    ) = recordSelectOnConditionStep(db).where(
        SCHOOL_USER.SCHOOL_ID.eq(schoolId).and(
            SCHOOL_USER_PERIOD.PERIOD_ID.eq(periodId)
        ).and(USER.ID.eq(id))
    ).fetchAny()?.let { record ->
        val userId = record.get(USER.ID)
        val allUserRoles = if (userId?.equals(principalName.toBigDecimal()) == true) {
            schoolUserRolesService.getAllUserRoles(id)
        } else {
            schoolUserRolesService.getAllUserRolesForPeriodAndSchool(id, schoolId, periodId)
        }
        mapUserRecordToUserModel(record, allUserRoles)
    } ?: error("User with id:${id} does not exist in current school and period")

    fun getAllUserViewsBySchool(schoolId: BigDecimal, periodId: BigDecimal, dsl: DSLContext = db): List<UserView> {
        val rolesForSchoolGroupedByUserId =
            schoolUserRolesService.getAllApprovedRolesFromSchoolForPeriod(schoolId, periodId)
        return recordSelectOnConditionStep(dsl).where(
            SCHOOL_USER.SCHOOL_ID.eq(schoolId).and(
                SCHOOL_USER_PERIOD.PERIOD_ID.eq(periodId)
            )
        ).map {
            val userId = it.get(USER.ID, BigDecimal::class.java)
            mapToUserView(it, rolesForSchoolGroupedByUserId[userId] ?: emptyList())
        }
    }

    fun getAllTeachersWhichDoNotHaveSchoolClassForSchoolAndPeriod(schoolId: BigDecimal, periodId: BigDecimal) =
        recordSelectOnConditionStepJoinedWithUserRoles().where(SCHOOL_ROLE_PERIOD.PERIOD_ID.eq(periodId))
            .and(SCHOOL_ROLE_PERIOD.STATUS.eq(RequestStatus.APPROVED.name))
            .and(SCHOOL_USER_PERIOD.PERIOD_ID.eq(periodId))
            .and(SCHOOL_USER_PERIOD.STATUS.eq(RequestStatus.APPROVED.name))
            .and(SCHOOL_USER.SCHOOL_ID.eq(schoolId))
            .and(SCHOOL_USER_ROLE.SCHOOL_ID.eq(schoolId))
            .and(SCHOOL_USER_ROLE.ROLE.eq(SchoolRole.TEACHER.name))
            .andNot(
                SCHOOL_USER_ROLE.ID.`in`(
                    db.select(SCHOOL_CLASS.MAIN_TEACHER_ROLE_ID).from(SCHOOL_CLASS)
                        .where(SCHOOL_CLASS.SCHOOL_ID.eq(schoolId))
                        .and(SCHOOL_CLASS.SCHOOL_PERIOD_ID.eq(periodId))
                )
            ).fetch().distinctBy { it.get(USER.ID) }.map { mapToUserView(it, emptyList()) }

    fun getAllStudentsInSchoolClass(
        schoolClassId: BigDecimal,
        periodId: BigDecimal
    ): List<StudentView> =
        studentRecordSelectOnConditionStep()
            .where(
                STUDENT_SCHOOL_CLASS.SCHOOL_CLASS_ID.eq(schoolClassId).and(
                    SCHOOL_USER_PERIOD.PERIOD_ID.eq(periodId)
                        .and(SCHOOL_USER_PERIOD.STATUS.eq(RequestStatus.APPROVED.name))
                        .and(SCHOOL_ROLE_PERIOD.PERIOD_ID.eq(periodId))
                        .and(SCHOOL_ROLE_PERIOD.STATUS.eq(RequestStatus.APPROVED.name))
                )
            ).orderBy(STUDENT_SCHOOL_CLASS.NUMBER_IN_CLASS)
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

    fun getStudentByIdAndSchoolClass(
        studentId: BigDecimal,
        schoolClassId: BigDecimal,
        periodId: BigDecimal
    ): StudentView? =
        studentRecordSelectOnConditionStep()
            .where(
                STUDENT_SCHOOL_CLASS.SCHOOL_CLASS_ID.eq(schoolClassId)
                    .and(USER.ID.eq(studentId))
                    .and(
                        SCHOOL_USER_PERIOD.PERIOD_ID.eq(periodId)
                            .and(SCHOOL_USER_PERIOD.STATUS.eq(RequestStatus.APPROVED.name))
                            .and(SCHOOL_ROLE_PERIOD.PERIOD_ID.eq(periodId))
                            .and(SCHOOL_ROLE_PERIOD.STATUS.eq(RequestStatus.APPROVED.name))
                    )
            )
            .fetchAny()
            ?.map { record ->
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
        requestIdToSchoolRolePeriodIdToStatus: Map<BigDecimal, Pair<BigDecimal, RequestStatus?>>,
        dsl: DSLContext = db
    ): Map<BigDecimal, Pair<OneRoleUser, RequestStatus?>> {
        val records = recordSelectOnConditionStepJoinedWithUserRoles(dsl)
            .apply {
                if (requestIdToSchoolRolePeriodIdToStatus.isNotEmpty()) {
                    where(SCHOOL_ROLE_PERIOD.ID.`in`(requestIdToSchoolRolePeriodIdToStatus.values.map { it.first }))
                }
            }
            .fetch()
        val schoolRolePeriodIdToOneRoleUserMap = records.associate { record ->
            val schoolUserRole = schoolUserRolesService.mapToModel(record)
            val schoolRolePeriodId = record.get(SCHOOL_ROLE_PERIOD.ID)!!
            schoolRolePeriodId to mapUserRecordToOneRoleModel(
                record.into(UserRecord::class.java),
                schoolUserRole
            )
        }
        return requestIdToSchoolRolePeriodIdToStatus.mapValues { (_, schoolRolePeriodIdToOneRoleUser) ->
            schoolRolePeriodIdToOneRoleUserMap[schoolRolePeriodIdToOneRoleUser.first]!! to schoolRolePeriodIdToOneRoleUser.second
        }
    }

    fun findUsersByTheirSchoolUserPeriodIds(
        requestIdToSchoolUserPeriodIdToStatus: Map<BigDecimal, Pair<BigDecimal, RequestStatus?>>,
        dsl: DSLContext
    ): Map<BigDecimal, Pair<User, RequestStatus?>> {
        val records = recordSelectOnConditionStepJoinedWithUserRoles(dsl)
            .where(SCHOOL_USER_PERIOD.ID.`in`(requestIdToSchoolUserPeriodIdToStatus.values.map { it.first }))
            .fetch()

        val schoolUserPeriodIdToUserMap = records.associate { record ->
            val schoolUserPeriodId = record.get(SCHOOL_USER_PERIOD.ID)!!
            schoolUserPeriodId to mapUserRecordToUserModel(
                record,
                emptyList()
            )
        }
        return requestIdToSchoolUserPeriodIdToStatus.mapValues { (_, schoolRolePeriodIdToOneRoleUser) ->
            schoolUserPeriodIdToUserMap[schoolRolePeriodIdToOneRoleUser.first]!! to schoolRolePeriodIdToOneRoleUser.second
        }
    }

    fun findApprovedUsersByItsRoleIdsAndPeriodId(
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


    fun changeUserProfilePicture(profilePicture: ByteArray, userId: BigDecimal) {
        db.selectFrom(USER).where(USER.ID.eq(userId))
            .fetchAny()?.apply {
                this.profileImage = profilePicture
            }?.update()
    }

    fun getUserProfilePicture(userId: BigDecimal): ByteArray? {
        return db.select(USER.PROFILE_IMAGE).from(USER).where(USER.ID.eq(userId))
            .fetchAny()?.getValue(USER.PROFILE_IMAGE)

    }


}