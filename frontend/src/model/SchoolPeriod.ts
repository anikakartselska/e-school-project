export interface SchoolPeriod {
    id: number,
    startYear: string,
    endYear: string,
    firstSemester: number,
    secondSemester: number
}

export interface SchoolPeriodWithSchoolIds {
    id: number,
    startYear: string,
    endYear: string,
    firstSemester: number,
    secondSemester: number
    schoolIds: number[]
}