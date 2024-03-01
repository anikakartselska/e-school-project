import {UserSecurity} from "./User";

export type AuthenticationResponse = Success | Error

export class Success {
    user: UserSecurity

    constructor(user: UserSecurity) {
        this.user = user;
    }
}

export class Error {
    result: AuthenticationResult

    constructor(result: AuthenticationResult) {
        this.result = result;
    }
}

export enum AuthenticationResult {
    UNKNOWN_LDAP_USER = "UNKNOWN_LDAP_USER",
    UNKNOWN_RVM_USER = "UNKNOWN_RVM_USER",
    WRONG_PASSWORD = "WRONG_PASSWORD",
}