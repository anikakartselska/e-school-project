package com.nevexis.backend.schoolManagement.school_lessons

import com.nevexis.backend.schoolManagement.school_class.SchoolClass
import com.nevexis.backend.schoolManagement.school_period.Semester
import com.nevexis.backend.schoolManagement.school_schedule.WorkingHour
import com.nevexis.backend.schoolManagement.subject.Subject
import com.nevexis.backend.schoolManagement.users.UserView
import java.math.BigDecimal
import java.time.LocalDateTime

data class SchoolLesson(
    val id: BigDecimal,
    val startTimeOfLesson: LocalDateTime,
    val endTimeOfLesson: LocalDateTime,
    val subject: Subject,
    val teacher: UserView,
    val schoolClass: SchoolClass,
    val lessonTopic: String? = null,
    val room: Int,
    val taken: Boolean = false,
    val week: Int,
    val semester: Semester,
    val workingDay: WorkingHour,
    val status: SchoolLessonStatus
)

enum class SchoolLessonStatus {
    FREE, NORMAL, SUBSTITUTION
}