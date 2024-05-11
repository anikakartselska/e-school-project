import {UserView} from "./User";
import {Semester} from "./SchoolPeriod";
import {AssignmentValue} from "./AssignmentValue";

export interface Assignments {
    id: number,
    createdBy: UserView,
    createdOn: Date | null
    text: String,
    semester: Semester,
    assignmentType: AssignmentType,
    assignmentValue: AssignmentValue
}


export enum AssignmentType {
    EXAMINATION = "EXAMINATION", HOMEWORK = "HOMEWORK", EVENT = "EVENT"
}

export const assignmentTypePluralTranslation = {
    EXAMINATION: "Контролни работи", HOMEWORK: "Домашни работи", EVENT: "Събития"
}

export const assignmentTypeTranslation = {
    EXAMINATION: "Контролна работа", HOMEWORK: "Домашна работа", EVENT: "Събитие"
}