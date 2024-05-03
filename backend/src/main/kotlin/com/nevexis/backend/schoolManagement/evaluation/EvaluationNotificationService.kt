package com.nevexis.backend.schoolManagement.evaluation

import com.nevexis.backend.schoolManagement.email.NotificationService
import com.nevexis.backend.schoolManagement.email.TemplateType
import com.nevexis.backend.schoolManagement.school_period.Semester
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.`demo-project`.jooq.tables.records.EvaluationRecord
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class EvaluationNotificationService {

    @Autowired private lateinit var notificationService: NotificationService

    @Autowired
    private lateinit var userService: UserService
    fun sendEmailForEvaluationsCreation(
        evaluations: List<Evaluation>,
        periodId: BigDecimal,
        schoolId: BigDecimal
    ) {
        val newEvaluationsGroupedByStudentId = evaluations.groupBy { it.student.id.toBigDecimal() }

        val studentIds = newEvaluationsGroupedByStudentId.keys.toList()

        val studentIdToParentEmails = userService.getParentEmailsFromListOfStudentIds(studentIds, periodId, schoolId)
        newEvaluationsGroupedByStudentId.forEach { (_, evaluations) ->
            evaluations.forEach { evaluation ->
                studentIdToParentEmails[evaluation.student.id.toBigDecimal()]?.let { email ->
                    notificationService.sendNotification(
                        listOf(email, evaluation.student.email),
                        "НОВО оценяване",
                        TemplateType.EVALUATION_ENTER,
                        getContextForEvaluationCreation(evaluation)
                    )
                }
            }
        }
    }

    fun sendEmailForEvaluationsUpdate(
        evaluationsToPreviousState: List<Pair<Evaluation, EvaluationRecord>>,
        periodId: BigDecimal,
        schoolId: BigDecimal
    ) {
        val newEvaluationsGroupedByStudentId = evaluationsToPreviousState.groupBy { it.first.student.id }

        val studentIds = newEvaluationsGroupedByStudentId.keys.toList().map { it.toBigDecimal() }

        val studentIdToParentEmails = userService.getParentEmailsFromListOfStudentIds(studentIds, periodId, schoolId)
        newEvaluationsGroupedByStudentId.forEach { (_, evaluations) ->
            evaluations.forEach { evaluation ->
                studentIdToParentEmails[evaluation.first.student.id.toBigDecimal()]?.let { email ->
                    notificationService.sendNotification(
                        listOf(email, evaluation.first.student.email),
                        "Редакция на оценяване",
                        TemplateType.EVALUATION_UPDATE,
                        getContextForEvaluationUpdate(
                            evaluation.first,
                            Json.decodeFromString(evaluation.second.evaluationValue!!)
                        )
                    )
                }
            }
        }
    }

    private fun getContextForEvaluationUpdate(
        evaluation: Evaluation,
        oldEvaluationValue: EvaluationValue
    ): Map<String, String> {
        return mapOf(
            "evaluation2" to when (evaluation.evaluationType) {
                EvaluationType.FEEDBACK -> "Отзивът"
                EvaluationType.GRADE -> "Оценката"
                EvaluationType.ABSENCE -> "Отсъствието"
            },
            "evaluation" to when (evaluation.evaluationType) {
                EvaluationType.FEEDBACK -> "отзив"
                EvaluationType.GRADE -> "оценка"
                EvaluationType.ABSENCE -> "отсъствие"
            },
            "studentName" to "${evaluation.student.firstName} ${evaluation.student.lastName}",
            "evaluationValue" to getEvaluationValueText(oldEvaluationValue, evaluation.semester),
            "newEvaluationValue" to getEvaluationValueText(evaluation.evaluationValue, evaluation.semester),
        )
    }

    private fun getContextForEvaluationCreation(evaluation: Evaluation): Map<String, String> {
        return mapOf(
            "evaluation" to when (evaluation.evaluationType) {
                EvaluationType.FEEDBACK -> "отзив"
                EvaluationType.GRADE -> "оценка"
                EvaluationType.ABSENCE -> "отсъствие"
            },
            "evaluationDate" to evaluation.evaluationDate!!.formatDateTimeWithDots(),
            "studentName" to "${evaluation.student.firstName} ${evaluation.student.lastName}",
            "evaluationValue" to getEvaluationValueText(evaluation.evaluationValue, evaluation.semester),
            "subjectName" to evaluation.subject.name,
            "createdBy" to "${evaluation.createdBy.firstName} ${evaluation.createdBy.lastName} (Имейл: ${evaluation.createdBy.email})",
            "comment" to (evaluation.comment ?: "Без коментар")
        )
    }

    private fun getEvaluationValueText(
        evaluationValue: EvaluationValue,
        semester: Semester
    ) = when (evaluationValue) {
        is EvaluationValue.AbsenceValue -> "${
            if (evaluationValue.absence == Absence.WHOLE) {
                "цяло"
            } else {
                "половин"
            }
        } ${
            if (evaluationValue.excused) {
                "извинено"
            } else {
                "неизвинено"
            }
        } отсътвие"

        is EvaluationValue.FeedbackValue -> "отзив ${evaluationValue.feedback.translation}"
        is EvaluationValue.GradeValue -> if (evaluationValue.finalGrade == true) {
            "${
                when (semester) {
                    Semester.FIRST -> "Срочна оценка за 1-ви срок"
                    Semester.SECOND -> "Срочна оценка за 2-ри срок"
                    Semester.YEARLY -> "Годишна оценка"
                }
            } ${evaluationValue.grade.translation} (${evaluationValue.grade.value})"
        } else {
            "оценка ${evaluationValue.grade.translation} (${evaluationValue.grade.value})"
        }
    }

    fun LocalDateTime.formatDateTimeWithDots(): String {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        return this.format(formatter)
    }

}
