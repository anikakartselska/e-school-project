package com.nevexis.backend.schoolManagement.actions

import com.nevexis.backend.schoolManagement.users.UserView
import java.math.BigDecimal
import java.time.LocalDateTime

data class Actions(
    val id: Int,
    val executedTime: LocalDateTime,
    val executedBy: UserView,
    val periodId: BigDecimal,
    val schoolId: BigDecimal,
    val action: String
) {
    companion object {
        fun evaluationCreateNotificationText() =
            "Ученикът ##studentName##\n" +
                    "        получи ##evaluation## ##evaluationValue## по предмет ##subjectName##" +
                    "        от ##createdBy## с коментар: ##comment##."

        fun evaluationUpdateNotificationText() =
            "##evaluation2## на ученика ##studentName## по предмет ##subjectName## бе променено/а от ##evaluationValue## на ##newEvaluationValue##."

        fun evaluationDeleteNotificationText() =
            "##evaluation2## - ##evaluationValue## по предмет ##subjectName## на ученика ##studentName## бе изтрит/а."

        fun assignmentCreateNotificationText() =
            "##createdBy## създаде ##assignment## ##assignmentValue##."

        fun assignmentUpdateNotificationText() =
            " ##assignment2## бе променено/а от ##assignmentValue## на ##newAssignmentValue##."

        fun assignmentDeleteNotificationText() = "##assignment2## - ##assignmentValue## бе изтрит/а."

        fun examCancelNotificationText() =
            "Изпитът на ученика на тема ##examNote## ##studentName## по предмет ##subjectName## бе анулиран от ##createdBy##."
    }
}

data class PaginatedFetchingInformationDTO(
    val startRange: Int,
    val endRange: Int,
    val forUserId: BigDecimal,
    val periodId: BigDecimal,
    val schoolId: BigDecimal,
)