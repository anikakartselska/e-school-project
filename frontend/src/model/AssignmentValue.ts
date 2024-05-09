import {SchoolLesson} from "./SchoolLesson";

export type AssignmentValue = ExaminationValue | HomeworkValue | EventValue


export class ExaminationValue {
    lesson: SchoolLesson

    constructor(lesson: SchoolLesson) {
        this.lesson = lesson
    }
}

export class HomeworkValue {
    to: Date
    homeworkLesson: SchoolLesson

    constructor(to: Date, homeworkLesson: SchoolLesson) {
        this.to = to
        this.homeworkLesson = homeworkLesson
    }
}

export class EventValue {
    from: Date
    to: Date
    room: string

    constructor(from: Date, to: Date, room: string) {
        this.from = from
        this.to = to
        this.room = room
    }
}