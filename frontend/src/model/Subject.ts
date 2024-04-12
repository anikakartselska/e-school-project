import {UserView} from "./User";

export interface Subject {
    id: number,
    name: String,
    teacher: UserView,
    forClass: number
}


export interface SubjectWithSchoolClassInformation {
    id: number,
    name: string,
    schoolClass: string,
    schoolId: number,
    schoolPeriodId: number
}