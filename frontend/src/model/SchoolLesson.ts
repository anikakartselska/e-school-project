import {Subject} from "./Subject";
import {SchoolClass} from "./SchoolClass";
import {WorkingHour} from "./PlannedSchoolLesson";
import {Semester} from "./SchoolPeriod";
import {UserView} from "./User";

export interface SchoolLesson {
    id: number,
    startTimeOfLesson: string,
    endTimeOfLesson: string,
    subject: Subject,
    schoolClass: SchoolClass,
    lessonTopic: string | null
    room: number,
    taken: boolean
    week: number,
    semester: Semester,
    workingDay: WorkingHour
    teacher: UserView,
    status: SchoolLessonStatus
}

enum SchoolLessonStatus {
    FREE, NORMAL, SUBSTITUTION
}