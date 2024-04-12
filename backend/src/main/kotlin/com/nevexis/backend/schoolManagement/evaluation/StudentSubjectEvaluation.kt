package com.nevexis.backend.schoolManagement.evaluation

import com.nevexis.backend.schoolManagement.subject.Subject
import com.nevexis.backend.schoolManagement.users.StudentView

data class StudentSubjectEvaluation(
    val subject: Subject,
    val student: StudentView,
    val absences: List<Evaluation>
)