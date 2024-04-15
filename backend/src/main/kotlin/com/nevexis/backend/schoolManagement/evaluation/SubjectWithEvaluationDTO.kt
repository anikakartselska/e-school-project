package com.nevexis.backend.schoolManagement.evaluation

import com.nevexis.backend.schoolManagement.subject.Subject

data class SubjectWithEvaluationDTO(
    val subject: Subject,
    val absences: List<Evaluation>,
    val grades: List<Evaluation>,
    val feedbacks: List<Evaluation>
)