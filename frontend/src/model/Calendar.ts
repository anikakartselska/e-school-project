export interface Calendar {
    beginningOfYear: string | null,
    endOfFirstSemester: string | null,
    beginningOfSecondSemester: string | null,
    classToEndOfYearDate: Map<number, string>,
    firstSemesterWeeksCount: number | null,
    classToSecondSemesterWeeksCount: Map<number, number> | null,
    restDays: RestDay[],
    examDays: RestDay[],
    firstShiftSchedule: DailySchedule,
    secondShiftSchedule: DailySchedule
}

export interface Week {
    weekNumber: number,
    startDate: string,
    endDate: string
}

export interface RestDay {
    from: string,
    to: string,
    holidayName: string
}

export interface DailySchedule {
    startOfClasses: string,
    durationOfClasses: number,
    breakDuration: number
}
