import {UserView} from "./User";

export interface SchoolClass {
    id: number,
    name: string,
    mainTeacher: UserView,
    schoolId: number,
    schoolPeriodId: number,
    shifts: ShiftsForSemesters
}

export interface SchoolClassWithPlan {
    id: number | null,
    name: string,
    mainTeacher: UserView | null
    plan: Map<string, number>,
    shifts: ShiftsForSemesters
}


export interface ShiftsForSemesters {
    firstSemester: Shift,
    secondSemester: Shift,
}

export enum Shift {
    FIRST = "FIRST", SECOND = "SECOND"
}

export const shiftTranslation = {
    FIRST: "Първа смяна", SECOND: "Втора смяна"
}