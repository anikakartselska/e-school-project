import {useLocalStorage} from "@vueuse/core";
import {User, UserSecurity, UserView} from "../model/User";
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

export const getCurrentUserAsUserView = (): UserView => {
    const user = getCurrentUser()

    return <UserView>{
        id: user.id,
        email: user.email,
        firstName: user.firstName,
        middleName: user.middleName,
        lastName: user.lastName,
        username: user.username,
    }
}

export const userIsLoggedIn = (): boolean => localStorage.getItem('user') != null

export const currentUserHasRole = (role: SchoolUserRole): boolean => {
    const currentUser: UserSecurity = getCurrentUser()
    return currentUser != null && currentUser.role == role;
}
export const updateUserInLocalStorage = (user: UserSecurity) => localStorage.setItem('user', JSON.stringify(user));

export const updateOneRoleUserInLocalStorage = (user: User) => {
    const userRoleFromLocalStorage = getCurrentUser().role

    const userSecurity = <UserSecurity>{
        id: user.id,
        email: user.email,
        firstName: user.firstName,
        middleName: user.middleName,
        lastName: user.lastName,
        password: null,
        username: user.username,
        role: user.roles?.find(it => it.id == userRoleFromLocalStorage.id),
    }
    localStorage.setItem('user', JSON.stringify(userSecurity))
}