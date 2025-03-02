package com.nevexis.backend.schoolManagement.assignments.examination

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.`demo-project`.jooq.tables.records.ExamAnswersRecord
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.EXAM_ANSWERS
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
class ExamAnswersService : BaseService() {

    @Autowired
    private lateinit var userService: UserService

    fun saveUpdateExamAnswers(
        examAnswers: ExamAnswers,
        schoolId: BigDecimal,
        periodId: BigDecimal,
        examId: BigDecimal
    ): ExamAnswers {
        return (db.selectFrom(EXAM_ANSWERS)
            .where(EXAM_ANSWERS.ID.eq(examAnswers.id)).fetchAny() ?: db.newRecord(EXAM_ANSWERS))
            .apply {
                id = examAnswers.id ?: getExamAnswersSeq()
                submittedBy = examAnswers.submittedBy.id.toBigDecimal()
                submittedOn = examAnswers.submittedOn ?: LocalDateTime.now()
                this.schoolId = schoolId
                this.schoolPeriodId = periodId
                this.graded = if (examAnswers.graded) {
                    "Y"
                } else {
                    "N"
                }
                this.examId = examId
                this.answers = examAnswers.answers?.let { Json.encodeToString(it) }
            }.also {
                it.store()
            }
            .let {
                examAnswers.copy(id = it.id)
            }
    }

    fun getExamAnswersByExamIdAndSubmittedById(examId: BigDecimal, submittedBy: BigDecimal) =
        getExamAnswersSelectConditionStep().where(
            EXAM_ANSWERS.EXAM_ID.eq(examId).and(EXAM_ANSWERS.SUBMITTED_BY.eq(submittedBy))
        ).fetchAny()?.let { mapToInternalModel(it) }

    fun deleteExamAnswersByExamId(examId: BigDecimal) =
        db.deleteFrom(EXAM_ANSWERS).where(EXAM_ANSWERS.EXAM_ID.eq(examId)).execute()

    private fun getExamAnswersSelectConditionStep() =
        db.select(EXAM_ANSWERS.asterisk(), USER.asterisk()).from(EXAM_ANSWERS).leftJoin(USER)
            .on(USER.ID.eq(EXAM_ANSWERS.SUBMITTED_BY))

    fun mapToInternalModel(record: Record): ExamAnswers {
        val examAnswersRecord = record.into(ExamAnswersRecord::class.java)
        return ExamAnswers(
            id = examAnswersRecord.id,
            submittedBy = userService.mapToUserView(record.into(UserRecord::class.java), emptyList()),
            submittedOn = examAnswersRecord.submittedOn,
            answers = examAnswersRecord.answers?.let { Json.decodeFromString(it) },
            graded = examAnswersRecord.graded == "Y"
        )
    }

    fun getExamAnswersSeq(): BigDecimal =
        db.select(DSL.field("EXAM_ANSWERS_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }
}