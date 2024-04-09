export interface SchoolPeriod {
    id: number,
    startYear: string,
    endYear: string
}

export enum Semester {
    FIRST, SECOND
}

export interface SchoolPeriodWithSchoolIds {
    id: number,
    startYear: string,
    endYear: string,
    schoolIds: number[]
}