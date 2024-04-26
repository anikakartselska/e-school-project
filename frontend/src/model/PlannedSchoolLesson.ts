import {TeacherView} from "./User";
import {SchoolClassWithPlan} from "./SchoolClass";

export interface PlannedSchoolLesson {
    room: string,
    workingHour: WorkingHour,
    teacher: TeacherView,
    subject: string,
    schoolClass: SchoolClassWithPlan
}

export interface WorkingHour {
    workingDays: WorkingDays,
    hour: number
}

export enum WorkingDays {
    MONDAY = "MONDAY",
    TUESDAY = "TUESDAY",
    WEDNESDAY = "WEDNESDAY",
    THURSDAY = "THURSDAY",
    FRIDAY = "FRIDAY"
}