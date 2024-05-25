package com.nevexis.backend.schoolManagement.yearly_results

import com.nevexis.backend.schoolManagement.users.StudentView
import kotlinx.serialization.Serializable

@Serializable
data class YearlyResults(
    val result: ResultType,
    val resultAfterTakingResitExams: ResultType? = null,
)

@Serializable
data class StudentToYearlyResult(
    val studentView: StudentView,
    val yearlyResults: YearlyResults? = null
)

enum class ResultType {
    FINISHES, REMAINS, TAKES_RESIT_EXAMS
}