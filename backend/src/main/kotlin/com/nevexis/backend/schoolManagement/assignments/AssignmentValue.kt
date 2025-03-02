@file:UseSerializers(
    LocalDateTimeSerializer::class
)

package com.nevexis.backend.schoolManagement.assignments

import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.nevexis.backend.schoolManagement.school.RoomToSubjects
import com.nevexis.backend.schoolManagement.school_lessons.SchoolLesson
import com.nevexis.backend.serializers.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import java.time.LocalDateTime

@Serializable(with = AssignmentValueSerializer::class)
@JsonDeserialize(using = AssignmentValueDeserializer::class)
sealed class AssignmentValue {
    @Serializable
    data class ExaminationValue(
        val lesson: SchoolLesson,
        val exam: Int? = null
    ) : AssignmentValue()

    @Serializable
    data class HomeworkValue(
        val to: LocalDateTime,
        val homeworkLesson: SchoolLesson
    ) : AssignmentValue()

    @Serializable
    data class EventValue(
        val from: LocalDateTime,
        val to: LocalDateTime,
        val room: RoomToSubjects
    ) : AssignmentValue()
}

object AssignmentValueSerializer :
    JsonContentPolymorphicSerializer<AssignmentValue>(AssignmentValue::class) {
    override fun selectDeserializer(element: JsonElement) = when {
        "lesson" in element.jsonObject -> AssignmentValue.ExaminationValue.serializer()
        "homeworkLesson" in element.jsonObject -> AssignmentValue.HomeworkValue.serializer()
        "room" in element.jsonObject -> AssignmentValue.EventValue.serializer()
        else -> error("There are no other student evaluations")
    }
}

class AssignmentValueDeserializer : JsonDeserializer<AssignmentValue>() {
    override fun deserialize(
        p: com.fasterxml.jackson.core.JsonParser,
        ctxt: DeserializationContext
    ): AssignmentValue {
        val rootNode = p.codec.readTree<JsonNode>(p) ?: throw IllegalArgumentException("Unable to parse JSON")
        val evaluationNode = rootNode.path("assignmentValue")

        if (evaluationNode.isMissingNode) {
            throw IllegalArgumentException("Assignment value is missing in the JSON")
        }

        val evaluationJson = evaluationNode.toString()

        return when {
            evaluationJson.contains("lesson") -> Json.decodeFromString<AssignmentValue.ExaminationValue>(
                evaluationJson
            )

            evaluationJson.contains("homeworkLesson") -> Json.decodeFromString<AssignmentValue.HomeworkValue>(
                evaluationJson
            )

            else -> Json.decodeFromString<AssignmentValue.EventValue>(evaluationJson)
        }
    }
}