import {SchoolClass} from "./SchoolClass";

export interface User {
    id: number | null
    personalNumber: string | null
    email: string | null
    phoneNumber: string | null
    firstName: string | null
    middleName: string | null
    lastName: string | null
    username: string | null
    address: string | null
    role: SchoolRole[] | null
    details: DetailsForUser[] | null
    password: string | null
}

export interface UserView {
    id: number
    firstName: string,
    middleName: string,
    lastName: string,
    username: string,
    email: string,
    role: SchoolRole[]
}

export interface StudentView {
    id: number,
    email: string,
    firstName: string,
    middleName: string,
    lastName: string,
    username: string,
    numberInClass: string,
}

export enum SchoolRole {
    ADMIN = "ADMIN",
    TEACHER = "TEACHER",
    STUDENT = "STUDENT",
    PARENT = "PARENT",
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
    children: User | null

    constructor(children: User | null) {
        this.children = children
    }
}

export const isDetailsForParent = (detailsForUser: DetailsForUser): detailsForUser is DetailsForParent =>
        detailsForUser instanceof DetailsForParent || (detailsForUser && 'children' in detailsForUser)

export const isDetailsForStudent = (detailsForUser: DetailsForUser): detailsForUser is DetailsForStudent =>
        detailsForUser instanceof DetailsForStudent || (detailsForUser && 'numberInClass' in detailsForUser)


