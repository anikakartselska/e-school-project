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

export const workingDaysOrder = new Map<WorkingDays, number>([
    [WorkingDays.MONDAY, 1],
    [WorkingDays.TUESDAY, 2],
    [WorkingDays.WEDNESDAY, 3],
    [WorkingDays.THURSDAY, 4],
    [WorkingDays.FRIDAY, 5],
]);

export const workingDaysTranslation = new Map<WorkingDays, string>([
    [WorkingDays.MONDAY, "Понеделник"],
    [WorkingDays.TUESDAY, "Вторник"],
    [WorkingDays.WEDNESDAY, "Сряда"],
    [WorkingDays.THURSDAY, "Четвъртък"],
    [WorkingDays.FRIDAY, "Петък"],
]);
