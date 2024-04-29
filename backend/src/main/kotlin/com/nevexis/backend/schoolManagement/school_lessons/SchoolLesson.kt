package com.nevexis.backend.schoolManagement.school_lessons

import com.nevexis.backend.schoolManagement.school_class.SchoolClass
import com.nevexis.backend.schoolManagement.subject.Subject
import java.math.BigDecimal
import java.time.LocalDateTime

data class SchoolLesson(
    val id: BigDecimal,
    val startTimeOfLesson: LocalDateTime,
    val endTimeOfLesson: LocalDateTime,
    val subject: Subject,
    val schoolClass: SchoolClass,
    val lessonTopic: String,
    val room: Int,
    val taken: Boolean,
)