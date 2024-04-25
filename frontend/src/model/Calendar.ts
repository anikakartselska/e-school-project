export interface Calendar {
    beginningOfYear: string,
    endOfFirstSemester: string,
    beginningOfSecondSemester: string,
    classToEndOfYearDate: Map<number, string>,
    restDays: RestDay[],
}

export interface RestDay {
    from: string,
    to: string,
    holidayName: string
}