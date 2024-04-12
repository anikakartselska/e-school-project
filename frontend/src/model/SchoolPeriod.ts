export interface SchoolPeriod {
    id: number,
    startYear: string,
    endYear: string
}

export enum Semester {
    FIRST = "FIRST", SECOND = "SECOND", YEARLY = "YEARLY"
}

export interface SchoolPeriodWithSchoolIds {
    id: number,
    startYear: string,
    endYear: string,
    schoolIds: number[]
}