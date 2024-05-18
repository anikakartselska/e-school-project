import {SchoolClass} from "./SchoolClass";
import {StudentView} from "./User";
import {Pair} from "./Pair";

export interface SchoolStatistics {
    success: number,
    grades: number,
    absences: number,
    feedback: number,
    examinations: number,
    events: number,
    schoolClassToAverageGrade: Pair<SchoolClass, number | null>[],
    studentToAverageGrade: Pair<StudentView, number | null>[]
}