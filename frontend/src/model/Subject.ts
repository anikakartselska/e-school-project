import {UserView} from "./User";
import {SchoolClass} from "./SchoolClass";

export interface Subject {
    id: number,
    name: string,
    teacher: UserView
}


export interface SubjectWithSchoolClassInformation {
    id: number,
    subjectName: string,
    schoolClass: SchoolClass,
}