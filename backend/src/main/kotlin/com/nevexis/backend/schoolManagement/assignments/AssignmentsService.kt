package com.nevexis.backend.schoolManagement.assignments

import com.nevexis.backend.schoolManagement.BaseService
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

    fun saveUpdateAssignments(
        assignments: Assignments,
        schoolClassId: BigDecimal,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ): Assignments {
        val id = assignments.id?.toBigDecimal() ?: getAssignmentSeqNextVal()
        val newOrUpdatedAssigment =
            assignments.copy(id = id.toInt(), createdOn = assignments.createdOn ?: LocalDateTime.now())
        mapToAssignmentToAssignmentRecord(newOrUpdatedAssigment, schoolId, periodId, schoolClassId).store()
        return newOrUpdatedAssigment
    }

    fun deleteAssignments(
        assignmentsId: BigDecimal
    ) {
        db.selectFrom(ASSIGNMENTS)
            .where(ASSIGNMENTS.ID.eq(assignmentsId)).fetchAny()?.delete()
    }

    fun getAllAssignmentsForSchoolClassPeriodAndSchool(
        schoolId: BigDecimal,
        periodId: BigDecimal,
        schoolClassId: BigDecimal
    ): List<Assignments> = db.select(
        ASSIGNMENTS.asterisk(),
        USER.asterisk()
    )
        .from(ASSIGNMENTS)
        .leftJoin(USER)
        .on(USER.ID.eq(ASSIGNMENTS.CREATED_BY))
        .where(
            ASSIGNMENTS.SCHOOL_PERIOD_ID.eq(periodId)
                .and(ASSIGNMENTS.SCHOOL_ID.eq(schoolId))
                .and(ASSIGNMENTS.SCHOOL_CLASS_ID.eq(schoolClassId))
        ).fetch().map {
            mapToAssignmentModel(it)
        }


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
    ) = db.newRecord(ASSIGNMENTS, assignments).apply {
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

    fun getAssignmentSeqNextVal(): BigDecimal =
        db.select(DSL.field("EVALUATION_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }
}