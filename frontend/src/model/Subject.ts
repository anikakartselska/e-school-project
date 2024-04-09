import {UserView} from "./User";
import {Semester} from "./SchoolPeriod";

export interface Subject {
    id: number,
    name: String,
    teacher: UserView,
    forClass: number,
    semester: Semester
}


export interface SubjectWithSchoolClassInformation {
    id: number,
    name: string,
    schoolClass: string,
    schoolId: number,
    schoolPeriodId: number
}