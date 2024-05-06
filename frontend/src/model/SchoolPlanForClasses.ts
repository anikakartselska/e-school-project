import {SubjectAndClassesCount} from "./SubjectAndClassesCount";
import {SchoolClass} from "./SchoolClass";

export interface SchoolPlanForClasses {
    id: number | null,
    name: string,
    subjectAndClassesCount: SubjectAndClassesCount[],
    schoolClassesWithTheSchoolPlan: SchoolClass[]
}