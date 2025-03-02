package com.nevexis.backend.schoolManagement.assignments.examination

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
data class Questions(
    val questions: List<Question>
)

@Serializable(with = QuestionSerializer::class)
sealed class Question {

    abstract val questionUUID: String
    abstract val questionTitle: String
    abstract val questionDescription: String?
    abstract val points: Int

    @Serializable
    data class ChoiceQuestion(
        override val questionUUID: String,
        val possibleAnswersToIfCorrect: List<PossibleAnswersToIfCorrect>,
        override val questionTitle: String,
        override val questionDescription: String?,
        override val points: Int
    ) : Question()

    @Serializable
    data class OpenQuestion(
        override val questionUUID: String,
        override val questionTitle: String,
        override val questionDescription: String?,
        override val points: Int
    ) : Question()

}

@Serializable
data class PossibleAnswersToIfCorrect(
    val text: String,
    val uuid: String,
    val correct: Boolean = false
)

object QuestionSerializer : JsonContentPolymorphicSerializer<Question>(Question::class) {
    override fun selectDeserializer(element: JsonElement) = when {
        "possibleAnswersToIfCorrect" in element.jsonObject -> {
            println("Detected ChoiceQuestion")
            Question.ChoiceQuestion.serializer()
        }

        else -> {
            println("Detected OpenQuestion")
            Question.OpenQuestion.serializer()
        }
    }
}

//class QuestionDeserializer : JsonDeserializer<Question>() {
//    override fun deserialize(
//        p: com.fasterxml.jackson.core.JsonParser,
//        ctxt: DeserializationContext
//    ): Question {
//        val rootNode = p.codec.readTree<JsonNode>(p) ?: throw IllegalArgumentException("Unable to parse JSON")
//        val evaluationNode = rootNode.path("question")
//
//        if (evaluationNode.isMissingNode) {
//            throw IllegalArgumentException("Assignment value is missing in the JSON")
//        }
//
//        val questionJson = rootNode.toString()
//
//        return when {
//            questionJson.contains("possibleAnswersToIfCorrect") -> Json.decodeFromString<Question.ChoiceQuestion>(
//                questionJson
//            )
//
//            else -> Json.decodeFromString<Question.OpenQuestion>(questionJson)
//        }
//    }
//}