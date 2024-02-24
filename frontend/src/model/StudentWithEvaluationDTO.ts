import {Evaluation} from "./Evaluation";
import {StudentView} from "./User";

export interface StudentWithEvaluationDTO {
    student: StudentView,
    absences: Evaluation[],
    grades: Evaluation[],
    feedbacks: Evaluation[]
}