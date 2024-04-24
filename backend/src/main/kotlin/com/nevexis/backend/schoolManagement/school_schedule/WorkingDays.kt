package com.nevexis.backend.schoolManagement.school_schedule

enum class WorkingDays(val order: Int) {
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5)
}

data class WorkingHour(
    val workingDays: WorkingDays,
    val hour: Int
)