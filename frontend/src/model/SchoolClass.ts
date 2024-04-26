import {UserView} from "./User";

export interface SchoolClass {
    id: number,
    name: string,
    mainTeacher: UserView,
    schoolId: number,
    schoolPeriodId: number
}

export interface SchoolClassWithPlan {
    id: number | null,
    name: string,
    mainTeacher: UserView | null
    plan: Map<string, number>
}