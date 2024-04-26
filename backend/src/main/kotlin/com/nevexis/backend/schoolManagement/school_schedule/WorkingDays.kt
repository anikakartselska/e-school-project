package com.nevexis.backend.schoolManagement.school_schedule

import kotlinx.serialization.Serializable

@Serializable
data class WorkingHour(
    val workingDays: WorkingDays,
    val hour: Int
)

enum class WorkingDays(val order: Int) {
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5)
}

