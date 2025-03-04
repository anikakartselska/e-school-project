package com.nevexis.backend.schoolManagement.assignments.examination

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
data class Answers(
    val answers: List<Answer>
)

@Serializable(with = AnswerSerializer::class)
sealed class Answer {


    abstract val questionUUID: String

    @Serializable
    data class ChoiceQuestionAnswer(
        val questionAnswers: List<String>? = null,
        val points: Int? = null,
        override val questionUUID: String
    ) : Answer()

    @Serializable
    data class OpenQuestionAnswer(
        val openQuestionAnswer: String? = null,
        val points: Int? = null,
        override val questionUUID: String
    ) : Answer()

}

object AnswerSerializer : JsonContentPolymorphicSerializer<Answer>(Answer::class) {
    override fun selectDeserializer(element: JsonElement) = when {
        "questionAnswers" in element.jsonObject -> {
            Answer.ChoiceQuestionAnswer.serializer()
        }

        else -> {
            Answer.OpenQuestionAnswer.serializer()
        }
    }
}