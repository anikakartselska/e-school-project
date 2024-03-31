import {User, UserView} from "./User";
import {RequestStatus} from "./RequestStatus";

export type RequestValue = UserRegistration | Role

export class Request {
    id: number
    requestedByUser: UserView
    requestValue: RequestValue
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

export class UserRegistration {
    user: User
    status: RequestStatus | null

    constructor(user, status) {
        this.user = user
        this.status = status
    }
}

export class Role {
    oneRoleUser: User
    status: RequestStatus | null

    constructor(oneRoleUser, status) {
        this.oneRoleUser = oneRoleUser
        this.status = status
    }
}
