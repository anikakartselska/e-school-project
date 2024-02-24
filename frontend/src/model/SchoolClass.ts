import {UserView} from "./User";

export interface SchoolClass {
    id: number,
    name: string,
    mainTeacher: UserView,
    schoolId: number,
    schoolPeriodId: number
}