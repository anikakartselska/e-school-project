import {SchoolLesson} from "./SchoolLesson";
import {Assignments, AssignmentType} from "./Assignments";
import {dateTimeToBulgarianLocaleString} from "../utils";

export type AssignmentValue = ExaminationValue | HomeworkValue | EventValue


export class ExaminationValue {
    lesson: SchoolLesson

    constructor(lesson: SchoolLesson) {
        this.lesson = lesson
    }
}

export class HomeworkValue {
    to: string
    homeworkLesson: SchoolLesson

    constructor(to: string, homeworkLesson: SchoolLesson) {
        this.to = to
        this.homeworkLesson = homeworkLesson
    }
}

export class EventValue {
    from: string
    to: string
    room: string

    constructor(from: string, to: string, room: string) {
        this.from = from
        this.to = to
        this.room = room
    }
}

export const constructAssignmentValueMessage = (assignment: Assignments) => {
    let assignmentValueMessage = ''
    switch (assignment.assignmentType) {
        case AssignmentType.EXAMINATION: {
            assignmentValueMessage = ` - от ${(dateTimeToBulgarianLocaleString((assignment.assignmentValue as ExaminationValue).lesson.startTimeOfLesson))} до ${(dateTimeToBulgarianLocaleString((assignment.assignmentValue as ExaminationValue).lesson.endTimeOfLesson))}, място: ${(assignment.assignmentValue as ExaminationValue).lesson.room} стая, по: ${(assignment.assignmentValue as ExaminationValue).lesson.subject.name}`
            break;
        }
        case AssignmentType.HOMEWORK: {
            assignmentValueMessage = ` - краен срок: ${(dateTimeToBulgarianLocaleString((assignment.assignmentValue as HomeworkValue).to))}, по: ${(assignment.assignmentValue as HomeworkValue).homeworkLesson.subject.name}`
            break;
        }
        case AssignmentType.EVENT: {
            assignmentValueMessage = ` - от ${(dateTimeToBulgarianLocaleString((assignment.assignmentValue as EventValue).from))} до ${(dateTimeToBulgarianLocaleString((assignment.assignmentValue as EventValue).to))}, място: ${(assignment.assignmentValue as EventValue).room} стая`
            break;
        }
    }
    return assignmentValueMessage
}