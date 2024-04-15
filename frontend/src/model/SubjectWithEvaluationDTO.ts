import {Evaluation} from "./Evaluation";
import {Subject} from "./Subject";

export interface SubjectWithEvaluationDTO {
    subject: Subject | undefined,
    absences: Evaluation[],
    grades: Evaluation[],
    feedbacks: Evaluation[]
}