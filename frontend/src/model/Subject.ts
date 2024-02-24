import {UserView} from "./User";

export interface Subject {
    id: number,
    name: String,
    teacher: UserView,
    schoolId: number,
    schoolPeriodId: number
}

export interface SubjectWithSchoolClassInformation {
    id: number,
    name: string,
    schoolClass: string,
    schoolId: number,
    schoolPeriodId: number
}