export interface Evaluation {
    id: number,
    studentId: number,
    subjectId: number,
    schoolLessonId: number,
    evaluationDate: Date,
    evaluationType: EvaluationType,
    evaluationValue: EvaluationValue,
    schoolId: number,
    schoolPeriodId: number,
}

export type EvaluationValue = FeedbackValue | AbsenceValue | GradeValue


export class FeedbackValue {
    feedback: Feedback

    constructor(feedback: Feedback) {
        this.feedback = feedback
    }
}

export class AbsenceValue {
    absence: Absence

    constructor(absence: Absence) {
        this.absence = absence
    }
}

export class GradeValue {
    grade: Grade

    constructor(grade: Grade) {
        this.grade = grade
    }
}

export enum EvaluationType {
    GRADE = "GRADE", FEEDBACK = "FEEDBACK", ABSENCE = "ABSENCE",
}

export enum Feedback {
    OVERALL_PRAISE = "OVERALL_PRAISE",
    ACTIVE_PARTICIPATION = "ACTIVE_PARTICIPATION",
    EXCELLENT_PERFORMANCE = "EXCELLENT_PERFORMANCE",
    TASK_COMPLETION = "TASK_COMPLETION",
    CURIOSITY = "CURIOSITY",
    DILIGENCE = "DILIGENCE",
    PROGRESS = "PROGRESS",
    COMMUNICATIVENESS = "COMMUNICATIVENESS",
    SHARP_MIND = "SHARP_MIND",
    CONCENTRATION = "CONCENTRATION",
    CREATIVITY = "CREATIVITY",
    TEAMWORK = "TEAMWORK",
    LEADERSHIP = "LEADERSHIP",
    PATRIOTISM = "PATRIOTISM",
    TOLERANCE = "TOLERANCE",
    EMOTIONAL_INTELLIGENCE = "EMOTIONAL_INTELLIGENCE",
    PRESENTATION_SKILLS = "PRESENTATION_SKILLS",
    DIGITAL_SKILLS = "DIGITAL_SKILLS",
    MUSICAL_CULTURE = "MUSICAL_CULTURE",
    PHYSICAL_EDUCATION = "PHYSICAL_EDUCATION",
    OLYMPIAN = "OLYMPIAN",
    GENERAL_REMARK = "GENERAL_REMARK",
    POOR_DISCIPLINE = "POOR_DISCIPLINE",
    LACK_OF_ATTENTION = "LACK_OF_ATTENTION",
    OFFICIAL_NOTE = "OFFICIAL_NOTE",
    DISRESPECT = "DISRESPECT",
    AGGRESSION = "AGGRESSION",
    REMOVED_FROM_CLASS = "REMOVED_FROM_CLASS",
    TARDINESS = "TARDINESS",
    ABSENCE = "ABSENCE",
    WEAK_PERFORMANCE = "WEAK_PERFORMANCE",
    UNPREPARED = "UNPREPARED",
    NO_HOMEWORK = "NO_HOMEWORK",
    NO_STUDY_MATERIALS = "NO_STUDY_MATERIALS",
    NO_TEXTBOOKS = "NO_TEXTBOOKS",
    NO_TEAM_PARTICIPATION = "NO_TEAM_PARTICIPATION",
    NO_UNIFORM = "NO_UNIFORM",
}

export enum Absence {WHOLE = "WHOLE", HALF = "HALF"}

export enum Grade {
    BAD = "BAD",
    MEDIUM = "MEDIUM",
    GOOD = "GOOD",
    VERY_GOOD = "VERY_GOOD",
    EXCELLENT = "EXCELLENT"
}

