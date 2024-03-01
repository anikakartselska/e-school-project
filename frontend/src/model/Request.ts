import {OneRoleUser} from "./User";
import {RequestStatus} from "./RequestStatus";
import {SchoolUserRole} from "./SchoolUserRole";

export type Request = RoleRequest | RegistrationRequest

export class RegistrationRequest {
    id: number
    requestedByUser: OneRoleUser
    requestDate: Date
    requestStatus: RequestStatus
    resolvedByUser: OneRoleUser | null
    resolvedDate: Date | null

    constructor(id, requestedByUser, requestDate, requestStatus, resolvedByUser, resolvedDate) {
        this.id = id
        this.requestedByUser = requestedByUser
        this.requestDate = requestDate
        this.requestStatus = requestStatus
        this.resolvedByUser = resolvedByUser
        this.resolvedDate = resolvedDate
    }

}

export class RoleRequest {
    id: number
    requestedByUser: OneRoleUser
    requestDate: Date
    requestStatus: RequestStatus
    resolvedByUser: OneRoleUser | null
    resolvedDate: Date | null
    schoolUserRole: SchoolUserRole

    constructor(id, requestedByUser, requestDate, requestStatus, resolvedByUser, resolvedDate, schoolUserRole) {
        this.id = id
        this.requestedByUser = requestedByUser
        this.requestDate = requestDate
        this.requestStatus = requestStatus
        this.resolvedByUser = resolvedByUser
        this.resolvedDate = resolvedDate
        this.schoolUserRole = schoolUserRole
    }
}