import {Questions} from "./Questions";
import {UserView} from "./User";

export interface Exam {
    id: number | null,
    createdBy: UserView,
    createdOn: Date | null
    examNote: string,
    questions: Questions
}
