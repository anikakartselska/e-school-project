package com.nevexis.backend.schoolManagement.school

import kotlinx.serialization.Serializable


@Serializable
data class School(
    val id: Int,
    val schoolName: String,
    val city: String,
    val address: String,
    val rooms: List<RoomToSubjects>? = null
)

@Serializable
data class RoomToSubjects(
    val room: String,
    val subjects: List<String>
)