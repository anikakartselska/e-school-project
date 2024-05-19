export interface SchoolPeriod {
    id: number | null,
    startYear: number,
    endYear: number
}

export enum Semester {
    FIRST = "FIRST", SECOND = "SECOND", YEARLY = "YEARLY"
}

export interface SchoolPeriodWithSchoolIds {
    id: number,
    startYear: number,
    endYear: number,
    schoolIds: number[]
}