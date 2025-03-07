package com.nevexis.backend.schoolManagement.assignments.examination

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.assignments.AssignmentsService
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.`demo-project`.jooq.tables.records.ExamRecord
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.EXAM
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
class ExamService : BaseService() {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var assignmentsService: AssignmentsService

    @Autowired
    private lateinit var examAnswersService: ExamAnswersService

    fun saveUpdateExam(exam: Exam, schoolId: BigDecimal, periodId: BigDecimal): Exam {
        return (db.selectFrom(EXAM)
            .where(EXAM.ID.eq(exam.id)).fetchAny() ?: db.newRecord(EXAM))
            .apply {
                id = exam.id ?: getExamSeqNextVal()
                createdBy = exam.createdBy.id.toBigDecimal()
                createdOn = exam.createdOn ?: LocalDateTime.now()
                this.schoolId = schoolId
                this.schoolPeriodId = periodId
                this.examNote = exam.examNote
                this.questions = exam.questions?.let { Json.encodeToString(it) }
                this.gradingScale = exam.gradingScale?.let { Json.encodeToString(it) }
            }.also {
                it.store()
            }
            .let {
                exam.copy(id = it.id)
            }
    }

    fun getExam(examId: BigDecimal): Exam? {
        return getExamSelectConditionStep()
            .where(EXAM.ID.eq(examId)).fetchAny()?.let {
                mapToInternalModel(it)
            }
    }


    fun deleteExam(examId: BigDecimal) {
        assignmentsService.removeAssignmentsExam(examId)
        examAnswersService.deleteExamAnswersByExamId(examId)
        db.deleteFrom(EXAM).where(EXAM.ID.eq(examId)).execute()

    }


    private fun getExamSelectConditionStep() =
        db.select(EXAM.asterisk(), USER.asterisk()).from(EXAM).leftJoin(USER).on(USER.ID.eq(EXAM.CREATED_BY))

    fun mapToInternalModel(record: Record): Exam {
        val examRecord = record.into(ExamRecord::class.java)
        return Exam(
            id = examRecord.id,
            createdBy = userService.mapToUserView(record.into(UserRecord::class.java), emptyList()),
            createdOn = examRecord.createdOn,
            examNote = examRecord.examNote,
            questions = examRecord.questions?.let { Json.decodeFromString(it) },
            gradingScale = examRecord.gradingScale?.let { Json.decodeFromString(it) }
        )
    }

    fun getExamSeqNextVal(): BigDecimal =
        db.select(DSL.field("EXAM_SEQUENCE.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }
}