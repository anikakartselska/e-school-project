export interface Calendar {
    beginningOfYear: string,
    endOfFirstSemester: string,
    beginningOfSecondSemester: string,
    classToEndOfYearDate: Map<number, string>,
    firstSemesterWeeksCount: number,
    classToSecondSemesterWeeksCount: Map<number, number>,
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
