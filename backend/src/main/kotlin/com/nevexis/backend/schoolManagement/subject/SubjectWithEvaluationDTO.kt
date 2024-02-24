package com.nevexis.backend.schoolManagement.subject

import com.nevexis.backend.schoolManagement.evaluation.Evaluation

data class SubjectWithEvaluationDTO(
    val subject: Subject,
    val absences: List<Evaluation>,
    val grades: List<Evaluation>,
    val feedbacks: List<Evaluation>
)