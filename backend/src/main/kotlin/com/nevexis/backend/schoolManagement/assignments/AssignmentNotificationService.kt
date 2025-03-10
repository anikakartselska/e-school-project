package com.nevexis.backend.schoolManagement.assignments

import com.nevexis.backend.schoolManagement.actions.ActionType
import com.nevexis.backend.schoolManagement.actions.ActionsContentService
import com.nevexis.backend.schoolManagement.actions.ActivityStreamService
import com.nevexis.backend.schoolManagement.email.NotificationService
import com.nevexis.backend.schoolManagement.email.TemplateType
import com.nevexis.backend.schoolManagement.users.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class AssignmentNotificationService {
    @Autowired private lateinit var notificationService: NotificationService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var activityStreamService: ActivityStreamService

    @Autowired
    private lateinit var actionsContentService: ActionsContentService

    fun sendEmailForAssignmentCreation(
        assignment: Assignments,
        periodId: BigDecimal,
        schoolId: BigDecimal,
        schoolClassId: BigDecimal
    ) {
        val studentsFromSchoolClass = userService.getAllStudentsInSchoolClass(schoolClassId, periodId)
        val parentInformation = userService.getParentEmailsFromListOfStudentIds(
            studentsFromSchoolClass.map { it.id.toBigDecimal() },
            periodId,
            schoolId
        ).values

        val parentsEmails = parentInformation.map { it.second!! }.toList()
        val studentsEmails = studentsFromSchoolClass.map { it.email }
        val parentIds = parentInformation.map { it.first!! }.toList()

        val studentIds = studentsFromSchoolClass.map { it.id.toBigDecimal() }


        notificationService.sendNotification(
            studentsEmails.plus(parentsEmails),
            when (assignment.assignmentType) {
                AssignmentType.EVENT -> "Ново събитие"
                AssignmentType.EXAMINATION -> "Ново изпитване"
                AssignmentType.HOMEWORK -> "Нова домашна работа"
            },
            TemplateType.ASSIGNMENT_CREATE,
            getContextForAssignmentCreate(assignment)
        )

        activityStreamService.createAction(
            periodId = periodId,
            schoolId = schoolId,
            userId = assignment.createdBy.id.toBigDecimal(),
            forUserIds = studentIds.plus(parentIds),
            action = actionsContentService.constructActionsMessage(
                ActionType.ASSIGNMENT_CREATE,
                actionsContentService.getContextForAssignmentCreate(assignment)
            )
        )
    }

    fun sendEmailForAssignmentUpdate(
        assignment: Assignments,
        oldAssignmentValue: AssignmentValue,
        oldAssignmentText: String,
        periodId: BigDecimal,
        schoolId: BigDecimal,
        schoolClassId: BigDecimal
    ) {
        val studentsFromSchoolClass = userService.getAllStudentsInSchoolClass(schoolClassId, periodId)
        val parentInformation = userService.getParentEmailsFromListOfStudentIds(
            studentsFromSchoolClass.map { it.id.toBigDecimal() },
            periodId,
            schoolId
        ).values.toList().distinct()

        val parentsEmails = parentInformation.map { it.second!! }
        val parentIds = parentInformation.map { it.first!! }
        val studentsEmails = studentsFromSchoolClass.map { it.email }
        val studentIds = studentsFromSchoolClass.map { it.id.toBigDecimal() }

        notificationService.sendNotification(
            studentsEmails.plus(parentsEmails),
            when (assignment.assignmentType) {
                AssignmentType.EVENT -> "Промяна на събитие"
                AssignmentType.EXAMINATION -> "Промяна на изпитване"
                AssignmentType.HOMEWORK -> "Промяна на домашна работа"
            },
            TemplateType.ASSIGNMENT_UPDATE,
            getContextForAssignmentUpdate(assignment, oldAssignmentValue, oldAssignmentText)
        )

        activityStreamService.createAction(
            periodId = periodId,
            schoolId = schoolId,
            userId = assignment.createdBy.id.toBigDecimal(),
            forUserIds = studentIds.plus(parentIds),
            action = actionsContentService.constructActionsMessage(
                ActionType.ASSIGNMENT_UPDATE,
                actionsContentService.getContextForAssignmentUpdate(assignment, oldAssignmentValue, oldAssignmentText)
            )
        )
    }

    fun sendEmailForAssignmentDelete(
        assignment: Assignments,
        periodId: BigDecimal,
        schoolId: BigDecimal,
        schoolClassId: BigDecimal
    ) {
        val studentsFromSchoolClass = userService.getAllStudentsInSchoolClass(schoolClassId, periodId)
        val parentInformation = userService.getParentEmailsFromListOfStudentIds(
            studentsFromSchoolClass.map { it.id.toBigDecimal() },
            periodId,
            schoolId
        ).values.toList()

        val parentsEmails = parentInformation.map { it.second!! }
        val parentIds = parentInformation.map { it.first!! }
        val studentsEmails = studentsFromSchoolClass.map { it.email }
        val studentIds = studentsFromSchoolClass.map { it.id.toBigDecimal() }

        notificationService.sendNotification(
            studentsEmails.plus(parentsEmails),
            when (assignment.assignmentType) {
                AssignmentType.EVENT -> "Изтриване на събитие"
                AssignmentType.EXAMINATION -> "Изтриване на изпитване"
                AssignmentType.HOMEWORK -> "Изтриване на домашна работа"
            },
            TemplateType.ASSIGNMENT_DELETE,
            getContextForAssignmentDelete(assignment)
        )

        activityStreamService.createAction(
            periodId = periodId,
            schoolId = schoolId,
            userId = assignment.createdBy.id.toBigDecimal(),
            forUserIds = studentIds.plus(parentIds),
            action = actionsContentService.constructActionsMessage(
                ActionType.ASSIGNMENT_DELETE,
                actionsContentService.getContextForAssignmentDelete(assignment)
            )
        )
    }

    private fun getContextForAssignmentUpdate(
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

    private fun getContextForAssignmentCreate(
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
            "assignmentDate" to assignment.createdOn!!.formatDateTimeWithDots(),
            "createdBy" to "${assignment.createdBy.firstName} ${assignment.createdBy.lastName}",
        )
    }


    private fun getContextForAssignmentDelete(
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

    private fun getAssignmentValueText(
        assignmentValue: AssignmentValue,
        text: String
    ) = when (assignmentValue) {
        is AssignmentValue.EventValue -> "$text от ${assignmentValue.from.formatDateTimeWithDots()} до ${assignmentValue.to.formatDateTimeWithDots()}, място: ${assignmentValue.room.room} стая"
        is AssignmentValue.HomeworkValue -> "$text краен срок: ${assignmentValue.to.formatDateTimeWithDots()}, по: ${assignmentValue.homeworkLesson.subject.name}"
        is AssignmentValue.ExaminationValue -> "$text от ${assignmentValue.lesson.startTimeOfLesson.formatDateTimeWithDots()} до ${assignmentValue.lesson.endTimeOfLesson.formatDateTimeWithDots()}, място: ${assignmentValue.lesson.room.room} стая, по: ${assignmentValue.lesson.subject.name}"
    }

    fun LocalDateTime.formatDateTimeWithDots(): String {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        return this.format(formatter)
    }
}