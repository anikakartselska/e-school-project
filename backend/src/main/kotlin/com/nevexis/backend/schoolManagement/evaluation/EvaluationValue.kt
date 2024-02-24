package com.nevexis.backend.schoolManagement.evaluation

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import java.math.BigDecimal

@Serializable(with = EvaluationValueSerializer::class)
sealed class EvaluationValue {
    @Serializable
    data class FeedbackValue(
        val feedback: Feedback,
    ) : EvaluationValue()

    @Serializable
    data class AbsenceValue(
        val absence: Absence
    ) : EvaluationValue()

    @Serializable
    data class GradeValue(
        val grade: Grade
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

enum class Feedback(val positive: Boolean) {
    OVERALL_PRAISE(true),
    ACTIVE_PARTICIPATION(true),
    EXCELLENT_PERFORMANCE(true),
    TASK_COMPLETION(true),
    CURIOSITY(true),
    DILIGENCE(true),
    PROGRESS(true),
    COMMUNICATIVENESS(true),
    SHARP_MIND(true),
    CONCENTRATION(true),
    CREATIVITY(true),
    TEAMWORK(true),
    LEADERSHIP(true),
    PATRIOTISM(true),
    TOLERANCE(true),
    EMOTIONAL_INTELLIGENCE(true),
    PRESENTATION_SKILLS(true),
    DIGITAL_SKILLS(true),
    MUSICAL_CULTURE(true),
    PHYSICAL_EDUCATION(true),
    OLYMPIAN(true),
    GENERAL_REMARK(false),
    POOR_DISCIPLINE(false),
    LACK_OF_ATTENTION(false),
    OFFICIAL_NOTE(false),
    DISRESPECT(false),
    AGGRESSION(false),
    REMOVED_FROM_CLASS(false),
    TARDINESS(false),
    ABSENCE(false),
    WEAK_PERFORMANCE(false),
    UNPREPARED(false),
    NO_HOMEWORK(false),
    NO_STUDY_MATERIALS(false),
    NO_TEXTBOOKS(false),
    NO_TEAM_PARTICIPATION(false),
    NO_UNIFORM(false),
}

enum class Absence(val value: BigDecimal) {
    WHOLE(BigDecimal.ONE), HALF(BigDecimal(0.5))
}

enum class Grade(val value: BigDecimal) {
    BAD(2.toBigDecimal()),
    MEDIUM(3.toBigDecimal()),
    GOOD(4.toBigDecimal()),
    VERY_GOOD(5.toBigDecimal()),
    EXCELLENT(6.toBigDecimal()),
}

//-Обща похвала - OVERALL_PRAISE
//-Активно участие - ACTIVE_PARTICIPATION
//-Отлично представяне - EXCELLENT_PERFORMANCE
//-Изпълнена задача - TASK_COMPLETION
//-Любознание - CURIOSITY
//-Старание - DILIGENCE
//-Прогрес - PROGRESS
//-Комуникативност - COMMUNICATIVENESS
//-Бодър ум - SHARP_MIND
//-Концентрация - CONCENTRATION
//-Креативност - CREATIVITY
//-Работа в екип - TEAMWORK
//-Лидерство - LEADERSHIP
//-Родолюбие - PATRIOTISM
//-Толерантност - TOLERANCE
//-Емоционална интелигентност - EMOTIONAL_INTELLIGENCE
//-Презентационни умения - PRESENTATION_SKILLS
//-Дигитални умения - DIGITAL_SKILLS
//-Музикална култура - MUSICAL_CULTURE
//-Двигателна култура - PHYSICAL_EDUCATION
//-Олимпиец - OLYMPIAN

//-Обща забележка - GENERAL_REMARK
//-Лоша дисциплина - POOR_DISCIPLINE
//-Липса на внимание - LACK_OF_ATTENTION
//-Официална забележка - OFFICIAL_NOTE
//-Неуважение - DISRESPECT
//-Агресия - AGGRESSION
//-Отстранен от час - REMOVED_FROM_CLASS
//-Закъснение - TARDINESS
//-Отсъствие - ABSENCE
//-Слабо представяне - WEAK_PERFORMANCE
//-Без подготовка - UNPREPARED
//-Без домашна работа - NO_HOMEWORK
//-Без учебно помагало - NO_STUDY_MATERIALS
//-Без учебни пособия - NO_TEXTBOOKS
//-Без екип - NO_TEAM_PARTICIPATION
//-Без униформа - NO_UNIFORM