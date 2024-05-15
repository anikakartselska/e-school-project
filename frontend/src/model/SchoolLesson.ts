import {Subject} from "./Subject";
import {SchoolClass} from "./SchoolClass";
import {WorkingHour} from "./PlannedSchoolLesson";
import {Semester} from "./SchoolPeriod";
import {UserView} from "./User";
import {RoomToSubjects} from "./School";

export interface SchoolLesson {
    id: number,
    startTimeOfLesson: string,
    endTimeOfLesson: string,
    subject: Subject,
    schoolClass: SchoolClass,
    lessonTopic: string | null
    room: RoomToSubjects,
    taken: boolean
    week: number,
    semester: Semester,
    workingDay: WorkingHour
    teacher: UserView,
    status: SchoolLessonStatus
}

export enum SchoolLessonStatus {
    FREE = "FREE",
    NORMAL = "NORMAL",
    SUBSTITUTION = "SUBSTITUTION"
}

export const translationOfSchoolLessonStatus = {
    FREE: "Свободен час",
    NORMAL: "Нормален",
    SUBSTITUTION: "Заместник",
}
