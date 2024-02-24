package com.nevexis.backend.schoolManagement.evaluation

import com.nevexis.backend.schoolManagement.users.StudentView

data class StudentWithEvaluationDTO(
    val student: StudentView,
    val absences: List<Evaluation>,
    val grades: List<Evaluation>,
    val feedbacks: List<Evaluation>
)