package com.nevexis.backend.schoolManagement.assignments.examination

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.evaluation.EvaluationService
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

    @Autowired
    private lateinit var evaluationService: EvaluationService

    fun saveUpdateExamAnswers(
        examAnswers: ExamAnswers,
        schoolId: BigDecimal,
        periodId: BigDecimal,
        examId: BigDecimal,
        userId: BigDecimal
    ): ExamAnswers {
        return (db.selectFrom(EXAM_ANSWERS)
            .where(
                EXAM_ANSWERS.ID.eq(examAnswers.id?.toBigDecimal())
                    .or(EXAM_ANSWERS.SUBMITTED_BY.eq(userId).and(EXAM_ANSWERS.EXAM_ID.eq(examId)))
            ).fetchAny() ?: db.newRecord(EXAM_ANSWERS))
            .apply {
                id = examAnswers.id?.toBigDecimal() ?: getExamAnswersSeq()
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
                this.grade = examAnswers.grade?.toBigDecimal()
                this.inputtedGrade = if (examAnswers.inputtedGrade) {
                    "Y"
                } else {
                    "N"
                }
                this.submitted = if (examAnswers.submitted) {
                    "Y"
                } else {
                    "N"
                }
            }.also {
                it.store()
                if (examAnswers.graded && examAnswers.inputtedGrade) {
                    evaluationService.createEvaluationOnExamAnswers(listOf(examAnswers), schoolId, periodId, userId)
                }
            }
            .let {
                examAnswers.copy(id = it.id?.toInt())
            }
    }

    fun inputGradesToExamAnswers(
        listExamAnswers: List<ExamAnswers>,
        schoolId: BigDecimal,
        periodId: BigDecimal,
        userId: BigDecimal
    ): List<ExamAnswers> {
        val examAnswersForInput = listExamAnswers.filter { !it.inputtedGrade && it.graded && it.grade != null }

        if (examAnswersForInput.isEmpty()) {
            return emptyList()
        }

        return examAnswersForInput.map {
            it.copy(inputtedGrade = true)
        }.also { answers ->
            answers.map { it.mapToRecord(schoolId, periodId) }
                .also { records ->
                    db.batchUpdate(records).execute()
                    evaluationService.createEvaluationOnExamAnswers(answers, schoolId, periodId, userId)
                }
        }
    }


    fun getExamAnswersByExamIdAndSubmittedById(examId: BigDecimal, submittedBy: BigDecimal) =
        getExamAnswersSelectConditionStep().where(
            EXAM_ANSWERS.EXAM_ID.eq(examId).and(EXAM_ANSWERS.SUBMITTED_BY.eq(submittedBy))
        ).fetchAny()?.let { mapToInternalModel(it) }

    fun getExamAnswersForExam(examId: BigDecimal) =
        getExamAnswersSelectConditionStep().where(
            EXAM_ANSWERS.EXAM_ID.eq(examId).and(EXAM_ANSWERS.SUBMITTED.eq("Y"))
        ).fetch().map { mapToInternalModel(it) }

    fun deleteExamAnswersByExamId(examId: BigDecimal) =
        db.deleteFrom(EXAM_ANSWERS).where(EXAM_ANSWERS.EXAM_ID.eq(examId)).execute()

    fun getExamAnswer(id: BigDecimal) =
        getExamAnswersSelectConditionStep().where(
            EXAM_ANSWERS.ID.eq(id)
        ).fetchAny()?.map { mapToInternalModel(it) }

    private fun getExamAnswersSelectConditionStep() =
        db.select(EXAM_ANSWERS.asterisk(), USER.asterisk()).from(EXAM_ANSWERS).leftJoin(USER)
            .on(USER.ID.eq(EXAM_ANSWERS.SUBMITTED_BY))

    fun mapToInternalModel(record: Record): ExamAnswers {
        val examAnswersRecord = record.into(ExamAnswersRecord::class.java)
        return ExamAnswers(
            id = examAnswersRecord.id?.toInt(),
            submittedBy = userService.mapToUserView(record.into(UserRecord::class.java), emptyList()),
            submittedOn = examAnswersRecord.submittedOn,
            answers = examAnswersRecord.answers?.let { Json.decodeFromString(it) },
            graded = examAnswersRecord.graded == "Y",
            grade = examAnswersRecord.grade?.toInt(),
            inputtedGrade = examAnswersRecord.inputtedGrade == "Y",
            examId = examAnswersRecord.examId?.toInt()!!,
            submitted = examAnswersRecord.submitted == "Y",
        )
    }

    fun ExamAnswers.mapToRecord(schoolId: BigDecimal, periodId: BigDecimal): ExamAnswersRecord {
        return db.newRecord(EXAM_ANSWERS).apply {
            this.id = this@mapToRecord.id?.toBigDecimal()
            this.submittedBy = this@mapToRecord.submittedBy.id.toBigDecimal()
            this.submittedOn = this@mapToRecord.submittedOn
            this.schoolId = schoolId
            this.schoolPeriodId = periodId
            this.graded = if (this@mapToRecord.graded) {
                "Y"
            } else {
                "N"
            }
            this.examId = this@mapToRecord.examId.toBigDecimal()
            this.answers = this@mapToRecord.answers?.let { Json.encodeToString(it) }
            this.grade = this@mapToRecord.grade?.toBigDecimal()
            this.inputtedGrade = if (this@mapToRecord.inputtedGrade) {
                "Y"
            } else {
                "N"
            }
            this.submitted = if (this@mapToRecord.submitted) {
                "Y"
            } else {
                "N"
            }
        }
    }

    fun getExamAnswersSeq(): BigDecimal =
        db.select(DSL.field("EXAM_ANSWERS_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }
}