package com.nevexis.backend.schoolManagement.users.roles

import com.nevexis.backend.schoolManagement.requests.RequestStatus
import com.nevexis.backend.schoolManagement.school.School
import com.nevexis.backend.schoolManagement.school_period.SchoolPeriod
import com.nevexis.backend.schoolManagement.users.DetailsForUser
import com.nevexis.backend.schoolManagement.users.SchoolRole
import kotlinx.serialization.Serializable

@Serializable
data class SchoolUserRole(
    val id: Int? = null,
    val userId: Int? = null,
    val period: SchoolPeriod,
    val school: School,
    val role: SchoolRole,
    val status: RequestStatus = RequestStatus.PENDING,
    val detailsForUser: DetailsForUser? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SchoolUserRole

        if (userId != other.userId) return false
        if (period != other.period) return false
        if (school != other.school) return false
        if (role != other.role) return false
        if (status != other.status) return false
        // Compare stringified `detailsForUser`
        if (detailsForUser?.toString() != other.detailsForUser?.toString()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId?.hashCode() ?: 0
        result = 31 * result + period.hashCode()
        result = 31 * result + school.hashCode()
        result = 31 * result + role.hashCode()
        result = 31 * result + status.hashCode()
        // Use the stringified `detailsForUser` for hash code computation
        result = 31 * result + (detailsForUser?.toString().hashCode())
        return result
    }
}
