package com.nevexis.backend.schoolManagement.assignments

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.file_management.SmsFileService
import com.nevexis.backend.schoolManagement.school_period.Semester
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.`demo-project`.jooq.tables.records.AssignmentsRecord
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.ASSIGNMENTS
import com.nevexis.`demo-project`.jooq.tables.references.USER
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class AssignmentsService : BaseService() {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var assignmentNotificationService: AssignmentNotificationService

    @Autowired
    private lateinit var smsFileService: SmsFileService

    fun saveUpdateAssignments(
        assignments: Assignments,
        schoolClassId: BigDecimal,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ): Assignments {
        val id = assignments.id?.toBigDecimal() ?: getAssignmentSeqNextVal()
        val oldAssignment =
            selectOnConditionStep().where(ASSIGNMENTS.ID.eq(assignments.id?.toBigDecimal())).fetchAny()
                ?.into(AssignmentsRecord::class.java)

        val newOrUpdatedAssigment =
            assignments.copy(id = id.toInt(), createdOn = assignments.createdOn ?: LocalDateTime.now())

        mapToAssignmentToAssignmentRecord(
            newOrUpdatedAssigment,
            schoolId,
            periodId,
            schoolClassId
        ).also { it.store() }
        if (assignments.id == null) {
            assignmentNotificationService.sendEmailForAssignmentCreation(
                newOrUpdatedAssigment,
                periodId,
                schoolId,
                schoolClassId
            )
        } else {
            assignmentNotificationService.sendEmailForAssignmentUpdate(
                newOrUpdatedAssigment,
                Json.decodeFromString(oldAssignment!!.assignmentValue!!),
                oldAssignment.text!!,
                periodId,
                schoolId,
                schoolClassId
            )
        }

        return newOrUpdatedAssigment
    }

    fun deleteAssignments(
        assignments: Assignments,
        schoolClassId: BigDecimal,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ) {
        db.selectFrom(ASSIGNMENTS)
            .where(ASSIGNMENTS.ID.eq(assignments.id?.toBigDecimal())).fetchAny()?.delete().also {
                assignmentNotificationService.sendEmailForAssignmentDelete(
                    assignments,
                    periodId,
                    schoolId,
                    schoolClassId
                )
                smsFileService.deleteAllFiles(assignmentIds = listOf(assignments.id!!.toBigDecimal()))

            }
    }

    fun getAssignmentsCountForSchoolClassPeriodAndSchool(
        schoolId: BigDecimal,
        periodId: BigDecimal,
        schoolClassId: BigDecimal,
        assignmentType: AssignmentType
    ) = db.fetchCount(
        ASSIGNMENTS,
        ASSIGNMENTS.ASSIGNMENT_TYPE.eq(assignmentType.name),
        ASSIGNMENTS.SCHOOL_ID.eq(schoolId),
        ASSIGNMENTS.SCHOOL_PERIOD_ID.eq(periodId),
        ASSIGNMENTS.SCHOOL_CLASS_ID.eq(schoolClassId)
    )

    fun getAssignmentsCountForPeriodAndSchool(
        schoolId: BigDecimal,
        periodId: BigDecimal,
        assignmentType: AssignmentType
    ) = db.fetchCount(
        ASSIGNMENTS,
        ASSIGNMENTS.ASSIGNMENT_TYPE.eq(assignmentType.name),
        ASSIGNMENTS.SCHOOL_ID.eq(schoolId),
        ASSIGNMENTS.SCHOOL_PERIOD_ID.eq(periodId)
    )

    fun getAllAssignmentsForSchoolClassPeriodAndSchool(
        schoolId: BigDecimal,
        periodId: BigDecimal,
        schoolClassId: BigDecimal,
        schoolLessonId: BigDecimal? = null
    ): List<Assignments> = selectOnConditionStep()
        .where(
            ASSIGNMENTS.SCHOOL_PERIOD_ID.eq(periodId)
                .and(ASSIGNMENTS.SCHOOL_ID.eq(schoolId))
                .and(ASSIGNMENTS.SCHOOL_CLASS_ID.eq(schoolClassId))
                .let {
                    if (schoolLessonId != null) {
                        it.and(
                            ASSIGNMENTS.ASSIGNMENT_VALUE.like("%esson\":{\"id\":${schoolLessonId},%")
                                .or(ASSIGNMENTS.ASSIGNMENT_TYPE.eq(AssignmentType.EVENT.name))
                        )
                    } else {
                        it
                    }
                }
        ).fetch().map {
            mapToAssignmentModel(it)
        }

    private fun selectOnConditionStep() = db.select(
        ASSIGNMENTS.asterisk(),
        USER.asterisk()
    )
        .from(ASSIGNMENTS)
        .leftJoin(USER)
        .on(USER.ID.eq(ASSIGNMENTS.CREATED_BY))


    private fun mapToAssignmentsToAssignmentRecords(
        assignments: List<Assignments>,
        schoolId: BigDecimal,
        periodId: BigDecimal,
        schoolClassId: BigDecimal
    ) = assignments.map { evaluation ->
        mapToAssignmentToAssignmentRecord(evaluation, schoolId, periodId, schoolClassId)
    }


    private fun mapToAssignmentToAssignmentRecord(
        assignments: Assignments,
        schoolId: BigDecimal,
        periodId: BigDecimal,
        schoolClassId: BigDecimal
    ) = (db.selectFrom(ASSIGNMENTS).where(ASSIGNMENTS.ID.eq(assignments.id?.toBigDecimal())).fetchAny() ?: db.newRecord(
        ASSIGNMENTS,
        assignments
    ))
        .apply {
            id = assignments.id?.toBigDecimal() ?: getAssignmentSeqNextVal()
            createdBy = assignments.createdBy.id.toBigDecimal()
            createdOn = assignments.createdOn
            this.schoolId = schoolId
            schoolPeriodId = periodId
            this.schoolClassId = schoolClassId
            text = assignments.text
            semester = assignments.semester.name
            assignmentType = assignments.assignmentType.name
            assignmentValue = Json.encodeToString(assignments.assignmentValue)
        }

    private fun mapToAssignmentModel(
        record: Record,
    ) = record.into(AssignmentsRecord::class.java).let {
        Assignments(
            id = it.id!!.toInt(),
            createdBy = userService.mapToUserView(record.into(UserRecord::class.java), emptyList()),
            createdOn = it.createdOn!!,
            text = it.text!!,
            semester = Semester.valueOf(it.semester!!),
            assignmentType = AssignmentType.valueOf(it.assignmentType!!),
            assignmentValue = Json.decodeFromString(it.assignmentValue!!)
        )
    }

    fun removeAssignmentsExam(examId: BigDecimal) {
        val updatedAssignments =
            db.selectFrom(ASSIGNMENTS).where(ASSIGNMENTS.ASSIGNMENT_VALUE.contains("\"exam\":${examId}"))
                .fetch().map {
                    it.apply {
                        assignmentValue =
                            Json.encodeToString(
                                (Json.decodeFromString(assignmentValue!!) as AssignmentValue.ExaminationValue).copy(
                                    exam = null
                                )
                            )
                    }
                }
        db.batchUpdate(updatedAssignments).execute()
    }

    fun getAssignmentSeqNextVal(): BigDecimal =
        db.select(DSL.field("EVALUATION_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }
}