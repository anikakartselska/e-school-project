import {UserView} from "./User";
import {SchoolClass} from "./SchoolClass";
import {Semester} from "./SchoolPeriod";
import {AssignmentValue} from "./AssignmentValue";

export interface Assignments {
    id: number,
    createdBy: UserView,
    createdOn: Date | null
    schoolClass: SchoolClass,
    text: String,
    semester: Semester,
    assignmentType: AssignmentType,
    assignmentValue: AssignmentValue
}


export enum AssignmentType {
    EXAMINATION, HOMEWORK, EVENT
}