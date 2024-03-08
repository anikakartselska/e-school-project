import {SchoolClass} from "./SchoolClass";
import {SchoolUserRole} from "./SchoolUserRole";

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
    roles: SchoolRole[]
}

export interface StudentView {
    id: number,
    email: string,
    firstName: string,
    middleName: string,
    lastName: string,
    username: string,
    numberInCass: string,
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
    roles: SchoolUserRole[] | null
}

export interface UserSecurity {
    id: number,
    email: string,
    firstName: string,
    middleName: string,
    lastName: string,
    password: string,
    username: string,
    role: SchoolUserRole
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
        | DetailsForParent

export class DetailsForStudent {
    schoolClass: SchoolClass | null
    numberInClass: number | null

    constructor(schoolClass: SchoolClass | null, numberInClass: number | null) {
        this.schoolClass = schoolClass
        this.numberInClass = numberInClass
    }

}

export class DetailsForParent {
    child: OneRoleUser | null

    constructor(child: OneRoleUser | null) {
        this.child = child
    }
}

export const isDetailsForParent = (detailsForUser: DetailsForUser): detailsForUser is DetailsForParent =>
        detailsForUser instanceof DetailsForParent || (detailsForUser && 'children' in detailsForUser)

export const isDetailsForStudent = (detailsForUser: DetailsForUser): detailsForUser is DetailsForStudent =>
        detailsForUser instanceof DetailsForStudent || (detailsForUser && 'numberInClass' in detailsForUser)