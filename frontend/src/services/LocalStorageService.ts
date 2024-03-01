import {useLocalStorage} from "@vueuse/core";
import {UserSecurity} from "../model/User";
import {SchoolUserRole} from "../model/SchoolUserRole";

export const storeUser = (user: UserSecurity) => {
    if (localStorage.getItem('user')) {
        localStorage.setItem('user', JSON.stringify(user))
    } else {
        useLocalStorage('user', user);
    }
}
export const clearUserStorage = () => localStorage.clear()

export const getCurrentUser = (): UserSecurity => JSON.parse(<string>localStorage.getItem('user')) as UserSecurity

export const userIsLoggedIn = (): boolean => localStorage.getItem('user') != null

export const currentUserHasRole = (role: SchoolUserRole): boolean => {
    const currentUser: UserSecurity = getCurrentUser()
    return currentUser != null && currentUser.role == role;
}
export const updateUserInLocalStorage = (user: UserSecurity) => localStorage.setItem('user', JSON.stringify(user));