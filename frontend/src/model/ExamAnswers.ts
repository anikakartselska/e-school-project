import {UserView} from "./User";
import {Answers} from "./Answers";

export interface ExamAnswers {
    id: number | null
    submittedBy: UserView,
    submittedOn: Date | null,
    answers: Answers | null,
    graded: boolean | false,
    grade: number | null
    inputtedGrade: boolean | false
    examId: number
    submitted: boolean | false
    cancelled: boolean | false
}