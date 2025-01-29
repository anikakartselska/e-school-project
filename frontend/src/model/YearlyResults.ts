import {StudentView} from "./User";

export interface YearlyResults {
    result: ResultType,
    resultAfterTakingResitExams: ResultType | null,
}

export interface StudentToYearlyResult {
    studentView: StudentView,

    yearlyResults: YearlyResults | null
}

export enum ResultType {
    FINISHES = 'FINISHES', REMAINS = 'REMAINS', TAKES_RESIT_EXAMS = 'TAKES_RESIT_EXAMS'
}

export const translationOfResultType = {
    FINISHES: "Завършва",
    REMAINS: "Остава",
    TAKES_RESIT_EXAMS: "Полага поправителни изпити",
}

export const getResultTypeColorClass = (resultType: ResultType | undefined) => {
    switch (resultType) {
        case ResultType.FINISHES:
            return `bg-secondary`;
        case ResultType.TAKES_RESIT_EXAMS:
            return 'bg-primary';
        case ResultType.REMAINS:
            return 'bg-negative';
    }
    return ''
}
