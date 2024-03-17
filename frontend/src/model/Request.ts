import {OneRoleUser, User, UserView} from "./User";
import {RequestStatus} from "./RequestStatus";

export type Request = RoleRequest | RegistrationRequest

export class RegistrationRequest {
    id: number
    requestedByUser: UserView
    requestValue: User
    requestDate: Date
    requestStatus: RequestStatus
    resolvedByUser: UserView | null
    resolvedDate: Date | null

    constructor(id, requestedByUser, requestValue, requestDate, requestStatus, resolvedByUser, resolvedDate) {
        this.id = id
        this.requestedByUser = requestedByUser
        this.requestValue = requestValue
        this.requestDate = requestDate
        this.requestStatus = requestStatus
        this.resolvedByUser = resolvedByUser
        this.resolvedDate = resolvedDate
    }

}

export class RoleRequest {
    id: number
    requestedByUser: UserView
    requestValue: OneRoleUser
    requestDate: Date
    requestStatus: RequestStatus
    resolvedByUser: UserView | null
    resolvedDate: Date | null

    constructor(id, requestedByUser, requestValue, requestDate, requestStatus, resolvedByUser, resolvedDate) {
        this.id = id
        this.requestedByUser = requestedByUser
        this.requestValue = requestValue
        this.requestDate = requestDate
        this.requestStatus = requestStatus
        this.resolvedByUser = resolvedByUser
        this.resolvedDate = resolvedDate
    }
}