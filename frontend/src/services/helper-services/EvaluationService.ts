import {
    Absence,
    AbsenceValue,
    Evaluation,
    EvaluationType,
    Feedback,
    FeedbackValue,
    Grade,
    GradeValue
} from "../../model/Evaluation";

export const gradeBackgroundColorMap = new Map<Grade, string>([
    [Grade.BAD, 'bg-red-2'],
    [Grade.MEDIUM, 'bg-yellow-2'],
    [Grade.GOOD, 'bg-orange-2'],
    [Grade.VERY_GOOD, 'bg-blue-2'],
    [Grade.EXCELLENT, 'bg-green-2'],
]);

export const getAbsenceBackgroundColor = (evaluation: Evaluation) => {
    if (evaluation.evaluationType == EvaluationType.ABSENCE) {
        switch ((<AbsenceValue>evaluation.evaluationValue).absence) {
            case Absence.WHOLE: {
                switch ((<AbsenceValue>evaluation.evaluationValue).excused) {
                    case true:
                        return 'bg-green-2'
                    case false:
                        return 'bg-red-2'
                }
                break;
            }
            case Absence.HALF: {
                switch ((<AbsenceValue>evaluation.evaluationValue).excused) {
                    case true:
                        return 'bg-green-1'
                    case false:
                        return 'bg-red-1'
                }
                break;
            }
        }
    } else {
        return ''
    }
}
export const absenceBackgroundColorMap = new Map<Absence, string>([
    [Absence.HALF, 'bg-red-2'],
    [Absence.WHOLE, 'bg-red-4'],
]);

export const absenceMap = new Map<Absence, number>([
    [Absence.HALF, 0.5],
    [Absence.WHOLE, 1],
]);

export const gradeMap = new Map<Grade, number>([
    [Grade.BAD, 2],
    [Grade.MEDIUM, 3],
    [Grade.GOOD, 4],
    [Grade.VERY_GOOD, 5],
    [Grade.EXCELLENT, 6],
]);

export const getAverageGradeColorClass = (grade: number) => {
    if (grade >= 2 && grade < 3) {
        return 'bg-red-2'
    }
    if (grade >= 3 && grade < 4) {
        return 'bg-yellow-2'
    }
    if (grade >= 4 && grade < 5) {
        return 'bg-orange-2'
    }
    if (grade >= 5 && grade < 6) {
        return 'bg-blue-2'
    }
    if (grade == 6) {
        return 'bg-green-2'
    }
    return ''
}
export const calculateAbsencesSum = (evaluations: Evaluation[], excused: boolean | null = null) => {
    const absences = evaluations?.filter(it => (it.evaluationType == EvaluationType.ABSENCE) && (excused == null || excused == (<AbsenceValue>it.evaluationValue).excused))
    return absences?.map(absence => {
        return absenceMap.get((absence.evaluationValue as AbsenceValue).absence)!!
    }).reduce((sum, current) => sum + current, 0)
}
export const countFeedbacksSum = (evaluations: Evaluation[], positive: boolean | null = null) => {
    const feedbacks = evaluations?.filter(it => (it.evaluationType == EvaluationType.FEEDBACK) && (positive == null || positive == feedbacksMap.get((<FeedbackValue>it.evaluationValue).feedback)))
    return feedbacks?.length
}
export const calculateAverageGrade = (evaluations: Evaluation[], finalGrades: boolean = false) => {
    const grades = evaluations?.filter(it => it.evaluationType == EvaluationType.GRADE && ((finalGrades && (it.evaluationValue as GradeValue).finalGrade) || (!finalGrades && !(it.evaluationValue as GradeValue).finalGrade)))
    const sumOfGrades = grades?.map(grade => {
        return gradeMap.get((grade.evaluationValue as GradeValue).grade)!!
    }).reduce((sum, current) => sum + current, 0)
    return (sumOfGrades / grades?.length).toPrecision(2)
}

export const feedbacksMap = new Map<Feedback, boolean>([
    [Feedback.OVERALL_PRAISE, true],
    [Feedback.ACTIVE_PARTICIPATION, true],
    [Feedback.EXCELLENT_PERFORMANCE, true],
    [Feedback.TASK_COMPLETION, true],
    [Feedback.CURIOSITY, true],
    [Feedback.DILIGENCE, true],
    [Feedback.PROGRESS, true],
    [Feedback.COMMUNICATIVENESS, true],
    [Feedback.SHARP_MIND, true],
    [Feedback.CONCENTRATION, true],
    [Feedback.CREATIVITY, true],
    [Feedback.TEAMWORK, true],
    [Feedback.LEADERSHIP, true],
    [Feedback.PATRIOTISM, true],
    [Feedback.TOLERANCE, true],
    [Feedback.EMOTIONAL_INTELLIGENCE, true],
    [Feedback.PRESENTATION_SKILLS, true],
    [Feedback.DIGITAL_SKILLS, true],
    [Feedback.MUSICAL_CULTURE, true],
    [Feedback.PHYSICAL_EDUCATION, true],
    [Feedback.OLYMPIAN, true],
    [Feedback.GENERAL_REMARK, false],
    [Feedback.POOR_DISCIPLINE, false],
    [Feedback.LACK_OF_ATTENTION, false],
    [Feedback.OFFICIAL_NOTE, false],
    [Feedback.DISRESPECT, false],
    [Feedback.AGGRESSION, false],
    [Feedback.REMOVED_FROM_CLASS, false],
    [Feedback.TARDINESS, false],
    [Feedback.ABSENCE, false],
    [Feedback.WEAK_PERFORMANCE, false],
    [Feedback.UNPREPARED, false],
    [Feedback.NO_HOMEWORK, false],
    [Feedback.NO_STUDY_MATERIALS, false],
    [Feedback.NO_TEXTBOOKS, false],
    [Feedback.NO_TEAM_PARTICIPATION, false],
    [Feedback.NO_UNIFORM, false],
]);

export const feedbacksMapTranslation = new Map<Feedback, string>([
    [Feedback.OVERALL_PRAISE, "Обща похвала"],
    [Feedback.ACTIVE_PARTICIPATION, "Активно участие"],
    [Feedback.EXCELLENT_PERFORMANCE, "Отлично представяне"],
    [Feedback.TASK_COMPLETION, "Изпълнена задача"],
    [Feedback.CURIOSITY, "Любознание"],
    [Feedback.DILIGENCE, "Старание"],
    [Feedback.PROGRESS, "Прогрес"],
    [Feedback.COMMUNICATIVENESS, "Комуникативност"],
    [Feedback.SHARP_MIND, "Бодър ум"],
    [Feedback.CONCENTRATION, "Концентрация"],
    [Feedback.CREATIVITY, "Креативност"],
    [Feedback.TEAMWORK, "Работа в екип"],
    [Feedback.LEADERSHIP, "Лидерство"],
    [Feedback.PATRIOTISM, "Родолюбие"],
    [Feedback.TOLERANCE, "Толерантност"],
    [Feedback.EMOTIONAL_INTELLIGENCE, "Емоционална интелигентност"],
    [Feedback.PRESENTATION_SKILLS, "Презентационни умения"],
    [Feedback.DIGITAL_SKILLS, "Дигитални умения"],
    [Feedback.MUSICAL_CULTURE, "Музикална култура"],
    [Feedback.PHYSICAL_EDUCATION, "Двигателна култура"],
    [Feedback.OLYMPIAN, "Олимпиец"],
    [Feedback.GENERAL_REMARK, "Обща забележка"],
    [Feedback.POOR_DISCIPLINE, "Лоша дисциплина"],
    [Feedback.LACK_OF_ATTENTION, "Липса на внимание"],
    [Feedback.OFFICIAL_NOTE, "Официална забележка"],
    [Feedback.DISRESPECT, "Неуважение"],
    [Feedback.AGGRESSION, "Агресия"],
    [Feedback.REMOVED_FROM_CLASS, "Отстранен от час"],
    [Feedback.TARDINESS, "Закъснение"],
    [Feedback.ABSENCE, "Отсъствие"],
    [Feedback.WEAK_PERFORMANCE, "Слабо представяне"],
    [Feedback.UNPREPARED, "Без подготовка"],
    [Feedback.NO_HOMEWORK, "Без домашна работа"],
    [Feedback.NO_STUDY_MATERIALS, "Без учебно помагало"],
    [Feedback.NO_TEXTBOOKS, "Без учебни пособия"],
    [Feedback.NO_TEAM_PARTICIPATION, "Без екип"],
    [Feedback.NO_UNIFORM, "Без униформа"],
]);