import {Absence, AbsenceValue, Evaluation, EvaluationType, Feedback, Grade, GradeValue} from "../../model/Evaluation";

export const gradeBackgroundColorMap = new Map<Grade, string>([
    [Grade.BAD, 'bg-red-2'],
    [Grade.MEDIUM, 'bg-yellow-2'],
    [Grade.GOOD, 'bg-orange-2'],
    [Grade.VERY_GOOD, 'bg-blue-2'],
    [Grade.EXCELLENT, 'bg-green-2'],
]);

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

export const calculateAbsencesSum = (evaluations: Evaluation[]) => {
    const absences = evaluations.filter(it => it.evaluationType == EvaluationType.ABSENCE)
    return absences.map(absence => {
        return absenceMap.get((absence.evaluationValue as AbsenceValue).absence)!!
    }).reduce((sum, current) => sum + current, 0)

}

export const calculateAverageGrade = (evaluations: Evaluation[]) => {
    const grades = evaluations?.filter(it => it.evaluationType == EvaluationType.GRADE && !(it.evaluationValue as GradeValue).finalGrade)
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

