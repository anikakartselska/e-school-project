package com.nevexis.backend.schoolManagement.assignments.examination

import com.nevexis.backend.error_handling.SMSError
import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.actions.ActionType
import com.nevexis.backend.schoolManagement.actions.ActionsContentService
import com.nevexis.backend.schoolManagement.actions.ActivityStreamService
import com.nevexis.backend.schoolManagement.assignments.AssignmentValue
import com.nevexis.backend.schoolManagement.assignments.Assignments
import com.nevexis.backend.schoolManagement.assignments.AssignmentsService
import com.nevexis.backend.schoolManagement.email.NotificationService
import com.nevexis.backend.schoolManagement.email.TemplateType
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
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class ExamAnswersService : BaseService() {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var evaluationService: EvaluationService

    @Autowired
    @Lazy
    private lateinit var examService: ExamService

    @Autowired
    @Lazy
    private lateinit var notificationService: NotificationService

    @Autowired
    @Lazy
    private lateinit var activityStreamService: ActivityStreamService

    @Autowired
    private lateinit var assignmentsService: AssignmentsService

    @Autowired
    private lateinit var actionsContentService: ActionsContentService
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
                this.cancelled = if (examAnswers.cancelled) {
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

    fun cancelExamAnswers(
        listExamAnswers: List<ExamAnswers>,
        schoolId: BigDecimal,
        periodId: BigDecimal,
        input2: Boolean,
        userId: BigDecimal,
        examId: BigDecimal
    ): List<ExamAnswers> {
        val examAnswersForInput = listExamAnswers.filter { !it.graded }

        if (examAnswersForInput.isEmpty()) {
            return emptyList()
        }

        return examAnswersForInput.map {
            if (input2) {
                it.copy(cancelled = true, inputtedGrade = true, graded = true, grade = 2)
            } else {
                it.copy(
                    cancelled = true
                )
            }
        }
            .also { answers ->
                answers.map { it.mapToRecord(schoolId, periodId) }
                    .also { records ->
                        db.batchUpdate(records).execute()
                        sendEmailForExamCancellation(answers, schoolId, periodId, examId)
                        if (input2) {
                            evaluationService.createEvaluationOnExamAnswers(answers, schoolId, periodId, userId)
                        }

                    }
            }
    }

    fun sendEmailForExamCancellation(
        listExamAnswers: List<ExamAnswers>,
        periodId: BigDecimal,
        schoolId: BigDecimal,
        examId: BigDecimal
    ) {
        val listExamAnswersGroupedByStudentId = listExamAnswers.groupBy { it.submittedBy.id.toBigDecimal() }
        val exam = examService.getExam(examId)!!
        val assignment = assignmentsService.getAssignmentsForExam(schoolId, periodId, examId)
        val studentIds = listExamAnswersGroupedByStudentId.keys.toList()

        val studentIdToParentInformation =
            userService.getParentEmailsFromListOfStudentIds(studentIds, periodId, schoolId)

        listExamAnswersGroupedByStudentId.forEach { (_, examAnswers) ->
            examAnswers.forEach { examAnswer ->
                studentIdToParentInformation[examAnswer.submittedBy.id.toBigDecimal()].let { idToEmail ->
                    notificationService.sendNotification(
                        listOfNotNull(idToEmail?.second, examAnswer.submittedBy.email),
                        "Анулиране на изпит",
                        TemplateType.EXAM_CANCEL,
                        getContextForExamCancel(exam, assignment, examAnswer)
                    )

                    activityStreamService.createAction(
                        periodId = periodId,
                        schoolId = schoolId,
                        userId = examAnswer.submittedBy.id.toBigDecimal(),
                        forUserIds = listOfNotNull(idToEmail?.first, examAnswer.submittedBy.id.toBigDecimal()),
                        action = actionsContentService.constructActionsMessage(
                            ActionType.EXAM_CANCEL,
                            getContextForExamCancel(exam, assignment, examAnswer)
                        )
                    )
                }
            }
        }
    }

    private fun getContextForExamCancel(
        exam: Exam,
        assignment: Assignments,
        examAnswers: ExamAnswers
    ): Map<String, String> {
        return mapOf(
            "examNote" to exam.examNote!!,
            "studentName" to "${examAnswers.submittedBy.firstName} ${examAnswers.submittedBy.lastName}",
            "subjectName" to (assignment.assignmentValue as AssignmentValue.ExaminationValue).lesson.subject.name,
            "createdBy" to "${exam.createdBy.firstName} ${exam.createdBy.lastName}",
        )
    }

    fun gradeExamAnswers(
        listExamAnswers: List<ExamAnswers>,
        examId: BigDecimal,
        schoolId: BigDecimal,
        periodId: BigDecimal
    ): List<ExamAnswers> {
        val examAnswersForInput = listExamAnswers.filter { !it.graded }

        if (examAnswersForInput.isEmpty()) {
            return emptyList()
        }
        val exam = examService.getExam(examId)!!
        if (exam.gradingScale == null) {
            throw SMSError("Невалидна скала за оценяване", "Скала за оценяване не съществува")
        }
        if (exam.questions?.questions?.sumOf { it.points } != exam.gradingScale.interval6.endingPoints.toInt()) {
            throw SMSError("Грешка при завършване на проверката", "Скалата за оценяване е невалидна")
        }
        return examAnswersForInput.map {
            val currentTakeExamPoints = (it.answers?.answers?.sumOf { it.points ?: 0 } ?: 0).toBigDecimal()
            val grade = when {
                exam.gradingScale.interval3.startingPoints <= currentTakeExamPoints &&
                        exam.gradingScale.interval3.endingPoints >= currentTakeExamPoints -> 3

                exam.gradingScale.interval4.startingPoints <= currentTakeExamPoints &&
                        exam.gradingScale.interval4.endingPoints >= currentTakeExamPoints -> 4

                exam.gradingScale.interval5.startingPoints <= currentTakeExamPoints &&
                        exam.gradingScale.interval5.endingPoints >= currentTakeExamPoints -> 5

                exam.gradingScale.interval6.startingPoints <= currentTakeExamPoints &&
                        exam.gradingScale.interval6.endingPoints >= currentTakeExamPoints -> 6

                else -> 2
            }

            it.copy(graded = true, grade = grade)
        }.also { answers ->
            answers.map { it.mapToRecord(schoolId, periodId) }
                .also { records ->
                    db.batchUpdate(records).execute()
//                        evaluationService.createEvaluationOnExamAnswers(answers, schoolId, periodId, userId)
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
            cancelled = examAnswersRecord.cancelled == "Y"
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
            this.cancelled = if (this@mapToRecord.cancelled) {
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