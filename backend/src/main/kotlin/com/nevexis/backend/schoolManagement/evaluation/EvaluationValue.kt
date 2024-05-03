package com.nevexis.backend.schoolManagement.evaluation

import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import java.math.BigDecimal

@Serializable(with = EvaluationValueSerializer::class)
@JsonDeserialize(using = EvaluationValueDeserializer::class)
sealed class EvaluationValue {
    @Serializable
    data class FeedbackValue(
        val feedback: Feedback,
    ) : EvaluationValue()

    @Serializable
    data class AbsenceValue(
        val absence: Absence,
        val excused: Boolean
    ) : EvaluationValue()

    @Serializable
    data class GradeValue(
        val grade: Grade,
        val finalGrade: Boolean? = null
    ) : EvaluationValue()
}

object EvaluationValueSerializer :
    JsonContentPolymorphicSerializer<EvaluationValue>(EvaluationValue::class) {
    override fun selectDeserializer(element: JsonElement) = when {
        "feedback" in element.jsonObject -> EvaluationValue.FeedbackValue.serializer()
        "absence" in element.jsonObject -> EvaluationValue.AbsenceValue.serializer()
        "grade" in element.jsonObject -> EvaluationValue.GradeValue.serializer()
        else -> error("There are no other student evaluations")
    }
}

class EvaluationValueDeserializer : JsonDeserializer<EvaluationValue>() {
    override fun deserialize(
        p: com.fasterxml.jackson.core.JsonParser,
        ctxt: DeserializationContext
    ): EvaluationValue {
        val rootNode = p.codec.readTree<JsonNode>(p) ?: throw IllegalArgumentException("Unable to parse JSON")
        val evaluationNode = rootNode.path("evaluationValue")

        if (evaluationNode.isMissingNode) {
            throw IllegalArgumentException("Evaluation value is missing in the JSON")
        }

        val evaluationJson = evaluationNode.toString()

        return when {
            evaluationJson.contains("feedback") -> Json.decodeFromString<EvaluationValue.FeedbackValue>(evaluationJson)
            evaluationJson.contains("absence") -> Json.decodeFromString<EvaluationValue.AbsenceValue>(evaluationJson)
            else -> Json.decodeFromString<EvaluationValue.GradeValue>(evaluationJson)
        }
    }
}

enum class Feedback(val positive: Boolean, val translation: String) {
    OVERALL_PRAISE(true, "Обща похвала"),
    ACTIVE_PARTICIPATION(true, "Активно участие"),
    EXCELLENT_PERFORMANCE(true, "Отлично представяне"),
    TASK_COMPLETION(true, "Изпълнена задача"),
    CURIOSITY(true, "Любознание"),
    DILIGENCE(true, "Старание"),
    PROGRESS(true, "Прогрес"),
    COMMUNICATIVENESS(true, "Комуникативност"),
    SHARP_MIND(true, "Бодър ум"),
    CONCENTRATION(true, "Концентрация"),
    CREATIVITY(true, "Креативност"),
    TEAMWORK(true, "Работа в екип"),
    LEADERSHIP(true, "Лидерство"),
    PATRIOTISM(true, "Родолюбие"),
    TOLERANCE(true, "Толерантност"),
    EMOTIONAL_INTELLIGENCE(true, "Емоционална интелигентност"),
    PRESENTATION_SKILLS(true, "Презентационни умения"),
    DIGITAL_SKILLS(true, "Дигитални умения"),
    MUSICAL_CULTURE(true, "Музикална култура"),
    PHYSICAL_EDUCATION(true, "Двигателна култура"),
    OLYMPIAN(true, "Олимпиец"),
    GENERAL_REMARK(false, "Обща забележка"),
    POOR_DISCIPLINE(false, "Лоша дисциплина"),
    LACK_OF_ATTENTION(false, "Липса на внимание"),
    OFFICIAL_NOTE(false, "Официална забележка"),
    DISRESPECT(false, "Неуважение"),
    AGGRESSION(false, "Агресия"),
    REMOVED_FROM_CLASS(false, "Отстранен от час"),
    TARDINESS(false, "Закъснение"),
    ABSENCE(false, "Отсъствие"),
    WEAK_PERFORMANCE(false, "Слабо представяне"),
    UNPREPARED(false, "Без подготовка"),
    NO_HOMEWORK(false, "Без домашна работа"),
    NO_STUDY_MATERIALS(false, "Без учебно помагало"),
    NO_TEXTBOOKS(false, "Без учебни пособия"),
    NO_TEAM_PARTICIPATION(false, "Без екип"),
    NO_UNIFORM(false, "Без униформа"),
}

enum class Absence(val value: BigDecimal) {
    WHOLE(BigDecimal.ONE), HALF(BigDecimal(0.5))
}

enum class Grade(val value: BigDecimal, val translation: String) {
    BAD(2.toBigDecimal(), "Слаб"),
    MEDIUM(3.toBigDecimal(), "Среден"),
    GOOD(4.toBigDecimal(), "Добър"),
    VERY_GOOD(5.toBigDecimal(), "Много добър"),
    EXCELLENT(6.toBigDecimal(), "Отличен"),
}

//-(Обща похвала) - OVERALL_PRAISE
//-(Активно участие) - ACTIVE_PARTICIPATION
//-(Отлично представяне) - EXCELLENT_PERFORMANCE
//-(Изпълнена задача) - TASK_COMPLETION
//-(Любознание) - CURIOSITY
//-(Старание) - DILIGENCE
//-(Прогрес) - PROGRESS
//-(Комуникативност) - COMMUNICATIVENESS
//-(Бодър ум) - SHARP_MIND
//-(Концентрация) - CONCENTRATION
//-(Креативност) - CREATIVITY
//-(Работа в екип) - TEAMWORK
//-(Лидерство) - LEADERSHIP
//-(Родолюбие) - PATRIOTISM
//-(Толерантност) - TOLERANCE
//-(Емоционална интелигентност) - EMOTIONAL_INTELLIGENCE
//-(Презентационни умения) - PRESENTATION_SKILLS
//-(Дигитални умения) - DIGITAL_SKILLS
//-(Музикална култура) - MUSICAL_CULTURE
//-(Двигателна култура) - PHYSICAL_EDUCATION
//-(Олимпиец) - OLYMPIAN

//-(Обща забележка) - GENERAL_REMARK
//-(Лоша дисциплина) - POOR_DISCIPLINE
//-(Липса на внимание) - LACK_OF_ATTENTION
//-(Официална забележка) - OFFICIAL_NOTE
//-(Неуважение) - DISRESPECT
//-(Агресия) - AGGRESSION
//-(Отстранен от час) - REMOVED_FROM_CLASS
//-(Закъснение) - TARDINESS
//-(Отсъствие) - ABSENCE
//-(Слабо представяне) - WEAK_PERFORMANCE
//-(Без подготовка) - UNPREPARED
//-(Без домашна работа) - NO_HOMEWORK
//-(Без учебно помагало) - NO_STUDY_MATERIALS
//-(Без учебни пособия) - NO_TEXTBOOKS
//-(Без екип) - NO_TEAM_PARTICIPATION
//-(Без униформа) - NO_UNIFORM