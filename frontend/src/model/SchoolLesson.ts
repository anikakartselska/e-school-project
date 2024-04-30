import {Subject} from "./Subject";
import {SchoolClass} from "./SchoolClass";
import {WorkingHour} from "./PlannedSchoolLesson";
import {Semester} from "./SchoolPeriod";

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
}