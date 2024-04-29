export interface Calendar {
    beginningOfYear: string,
    endOfFirstSemester: string,
    beginningOfSecondSemester: string,
    classToEndOfYearDate: Map<number, string>,
    restDays: RestDay[],
    firstShiftSchedule: DailySchedule,
    secondShiftSchedule: DailySchedule
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
