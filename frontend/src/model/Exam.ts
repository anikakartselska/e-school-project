import {Questions} from "./Questions";
import {UserView} from "./User";
import {GradingScale} from "./GradingScale";

export interface Exam {
    id: number | null,
    createdBy: UserView,
    createdOn: Date | null
    examNote: string,
    questions: Questions,
    gradingScale: GradingScale | null
}
