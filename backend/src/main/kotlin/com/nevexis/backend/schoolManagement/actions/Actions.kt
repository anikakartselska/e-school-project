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
            "Ученикът <span id=\"studentName\">##studentName##</span>\n" +
                    "        получи ##evaluation## <span id=\"evaluationValue\">##evaluationValue##</span> по предмет <span id=\"subjectName\">##subjectName##</span>\n" +
                    "        от <span id=\"createdByName\">##createdBy##</span> с коментар: \"<span id=\"comment\">##comment##</span>\"."

        fun evaluationUpdateNotificationText() =
            "  ##evaluation2## на ученика <span>##studentName##</span> по предмет <span>##subjectName##</span> бе променено/а от ##evaluationValue## на ##newEvaluationValue##."

        fun evaluationDeleteNotificationText() =
            "##evaluation2## - ##evaluationValue## по предмет <span>##subjectName##</span> на ученика <span>##studentName##</span> бе изтрит/а."

        fun assignmentCreateNotificationText() =
            "<span>##createdBy##</span> създаде ##assignment## ##assignmentValue##."

        fun assignmentUpdateNotificationText() =
            " ##assignment2## бе променено/а от ##assignmentValue## на ##newAssignmentValue##."

        fun assignmentDeleteNotificationText() = "##assignment2## - ##assignmentValue## бе изтрит/а."
    }
}

data class ActionsFetchingInformationDTO(
    val startRange: Int,
    val endRange: Int,
    val forUserId: BigDecimal,
    val periodId: BigDecimal,
    val schoolId: BigDecimal,
)