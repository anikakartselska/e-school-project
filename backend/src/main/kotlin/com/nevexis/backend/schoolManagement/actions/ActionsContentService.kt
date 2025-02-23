package com.nevexis.backend.schoolManagement.actions

import com.nevexis.backend.schoolManagement.assignments.AssignmentType
import com.nevexis.backend.schoolManagement.assignments.AssignmentValue
import com.nevexis.backend.schoolManagement.assignments.Assignments
import com.nevexis.backend.schoolManagement.evaluation.Absence
import com.nevexis.backend.schoolManagement.evaluation.Evaluation
import com.nevexis.backend.schoolManagement.evaluation.EvaluationType
import com.nevexis.backend.schoolManagement.evaluation.EvaluationValue
import com.nevexis.backend.schoolManagement.school_period.Semester
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ActionsContentService {

    fun constructActionsMessage(actionType: ActionType, context: Map<String, String>?): String {
        val content = when (actionType) {
            ActionType.EVALUATION_CREATE -> Actions.evaluationCreateNotificationText()
            ActionType.EVALUATION_UPDATE -> Actions.evaluationUpdateNotificationText()
            ActionType.EVALUATION_DELETE -> Actions.evaluationDeleteNotificationText()
            ActionType.ASSIGNMENT_CREATE -> Actions.assignmentCreateNotificationText()
            ActionType.ASSIGNMENT_UPDATE -> Actions.assignmentUpdateNotificationText()
            ActionType.ASSIGNMENT_DELETE -> Actions.assignmentDeleteNotificationText()
        }

        return context?.toList()?.fold(content) { html, (key, value) ->
            html.replace("##${key}##", value)
        } ?: ""
    }

    fun getContextForEvaluationUpdate(
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
            "subjectName" to evaluation.subject.name,
            "studentName" to "${evaluation.student.firstName} ${evaluation.student.lastName}",
            "evaluationValue" to getEvaluationValueText(oldEvaluationValue, evaluation.semester),
            "newEvaluationValue" to getEvaluationValueText(evaluation.evaluationValue, evaluation.semester),
        )
    }

    fun getContextForEvaluationCreation(evaluation: Evaluation): Map<String, String> {
        return mapOf(
            "evaluation" to when (evaluation.evaluationType) {
                EvaluationType.FEEDBACK -> "отзив"
                EvaluationType.GRADE -> "оценка"
                EvaluationType.ABSENCE -> "отсъствие"
            },
            "studentName" to "${evaluation.student.firstName} ${evaluation.student.lastName}",
            "evaluationValue" to getEvaluationValueText(evaluation.evaluationValue, evaluation.semester),
            "subjectName" to evaluation.subject.name,
            "createdBy" to "${evaluation.createdBy.firstName} ${evaluation.createdBy.lastName} (Имейл: ${evaluation.createdBy.email})",
            "comment" to (evaluation.comment ?: "Без коментар")
        )
    }

    fun getContextForEvaluationDelete(
        evaluation: Evaluation
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
            "subjectName" to evaluation.subject.name,
            "studentName" to "${evaluation.student.firstName} ${evaluation.student.lastName}",
            "evaluationValue" to getEvaluationValueText(evaluation.evaluationValue, evaluation.semester),
        )
    }

    fun getEvaluationValueText(
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

    fun getContextForAssignmentUpdate(
        assignment: Assignments,
        oldAssignmentValue: AssignmentValue,
        oldAssignmentText: String
    ): Map<String, String> {
        return mapOf(
            "assignment2" to when (assignment.assignmentType) {
                AssignmentType.EVENT -> "Събитието"
                AssignmentType.EXAMINATION -> "Изпитването"
                AssignmentType.HOMEWORK -> "Домашната работа"
            },
            "assignment" to when (assignment.assignmentType) {
                AssignmentType.EVENT -> "събитие"
                AssignmentType.EXAMINATION -> "изпитване"
                AssignmentType.HOMEWORK -> "домашна работа"
            },
            "assignmentValue" to getAssignmentValueText(oldAssignmentValue, oldAssignmentText),
            "newAssignmentValue" to getAssignmentValueText(assignment.assignmentValue, assignment.text),
        )
    }

    fun getContextForAssignmentCreate(
        assignment: Assignments
    ): Map<String, String> {
        return mapOf(
            "assignment2" to when (assignment.assignmentType) {
                AssignmentType.EVENT -> "Събитието"
                AssignmentType.EXAMINATION -> "Изпитването"
                AssignmentType.HOMEWORK -> "Домашната работа"
            },
            "assignment" to when (assignment.assignmentType) {
                AssignmentType.EVENT -> "събитие"
                AssignmentType.EXAMINATION -> "изпитване"
                AssignmentType.HOMEWORK -> "домашна работа"
            },
            "assignmentValue" to getAssignmentValueText(assignment.assignmentValue, assignment.text),
            "createdBy" to "${assignment.createdBy.firstName} ${assignment.createdBy.lastName}",
        )
    }

    fun getContextForAssignmentDelete(
        assignment: Assignments
    ): Map<String, String> {
        return mapOf(
            "assignment2" to when (assignment.assignmentType) {
                AssignmentType.EVENT -> "Събитието"
                AssignmentType.EXAMINATION -> "Изпитването"
                AssignmentType.HOMEWORK -> "Домашната работа"
            },
            "assignment" to when (assignment.assignmentType) {
                AssignmentType.EVENT -> "събитие"
                AssignmentType.EXAMINATION -> "изпитване"
                AssignmentType.HOMEWORK -> "домашна работа"
            },
            "assignmentValue" to getAssignmentValueText(assignment.assignmentValue, assignment.text),
        )
    }

    fun getAssignmentValueText(
        assignmentValue: AssignmentValue,
        text: String
    ) = when (assignmentValue) {
        is AssignmentValue.EventValue -> "$text от ${assignmentValue.from.formatDateTimeWithDots()} до ${assignmentValue.to.formatDateTimeWithDots()}, място: ${assignmentValue.room} стая"
        is AssignmentValue.HomeworkValue -> "$text краен срок: ${assignmentValue.to.formatDateTimeWithDots()}, по: ${assignmentValue.homeworkLesson.subject.name}"
        is AssignmentValue.ExaminationValue -> "$text от ${assignmentValue.lesson.startTimeOfLesson.formatDateTimeWithDots()} до ${assignmentValue.lesson.endTimeOfLesson.formatDateTimeWithDots()}, място: ${assignmentValue.lesson.room} стая, по: ${assignmentValue.lesson.subject.name}"
    }

    fun LocalDateTime.formatDateTimeWithDots(): String {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        return this.format(formatter)
    }
}