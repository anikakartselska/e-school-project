import {SchoolClass} from "./SchoolClass";
import {SchoolUserRole} from "./SchoolUserRole";
import {RequestStatus} from "./RequestStatus";
import {SubjectWithSchoolClassInformation} from "./Subject";

export interface OneRoleUser {
    id: number,
    firstName: string,
    middleName: string,
    lastName: string,
    username: string,
    personalNumber: string,
    email: string,
    phoneNumber: string,
    address: string,
    role: SchoolUserRole
}

export interface UserView {
    id: number,
    email: string,
    firstName: string,
    middleName: string,
    lastName: string,
    username: string,
    roles: SchoolRole[],
    status: RequestStatus
}

export interface StudentView {
    id: number,
    email: string,
    firstName: string,
    middleName: string,
    lastName: string,
    username: string,
    numberInClass: number | null,
    schoolClassId: number,
    schoolClassName: string
}

export interface User {
    id: number | null,
    firstName: string,
    middleName: string,
    lastName: string,
    username: string,
    personalNumber: string,
    gender: Gender,
    email: string,
    phoneNumber: string,
    address: string,
    password: string | null,
    roles: SchoolUserRole[] | null,
    status: RequestStatus
}

export interface UserSecurity {
    id: number,
    email: string,
    firstName: string,
    middleName: string,
    lastName: string,
    password: string | null,
    username: string,
    role: SchoolUserRole
}

export interface TeacherView {
    id: number,
    email: string,
    firstName: string,
    middleName: string,
    lastName: string,
    username: string,
    qualifiedSubjects: string[],
}

export enum SchoolRole {
    ADMIN = "ADMIN",
    TEACHER = "TEACHER",
    STUDENT = "STUDENT",
    PARENT = "PARENT",
}

export enum Gender {
    FEMALE = "FEMALE",
    MALE = "MALE",
}


export type DetailsForUser =
        DetailsForStudent
        | DetailsForParent | DetailsForTeacher

export class DetailsForStudent {
    schoolClass: SchoolClass | null
    numberInClass: number | null
    parents: UserView[] | null

    constructor(schoolClass: SchoolClass | null, numberInClass: number | null, parents: UserView[] | null) {
        this.schoolClass = schoolClass
        this.numberInClass = numberInClass
        this.parents = parents
    }

}

export class DetailsForParent {
    child: OneRoleUser | null

    constructor(child: OneRoleUser | null) {
        this.child = child
    }
}

export class DetailsForTeacher {
    qualifiedSubjects: string[]
    teachingSubjects: SubjectWithSchoolClassInformation[]

    constructor(subjects: string[], teachingSubjects: SubjectWithSchoolClassInformation[]) {
        this.qualifiedSubjects = subjects
        this.teachingSubjects = teachingSubjects
    }
}

export const isDetailsForParent = (detailsForUser: DetailsForUser): detailsForUser is DetailsForParent =>
        detailsForUser instanceof DetailsForParent || (detailsForUser && 'child' in detailsForUser)

export const isDetailsForStudent = (detailsForUser: DetailsForUser): detailsForUser is DetailsForStudent =>
        detailsForUser instanceof DetailsForStudent || (detailsForUser && 'numberInClass' in detailsForUser)

export const isDetailsForTeacher = (detailsForUser: DetailsForUser): detailsForUser is DetailsForTeacher =>
        detailsForUser instanceof DetailsForTeacher || (detailsForUser && 'subjects' in detailsForUser)