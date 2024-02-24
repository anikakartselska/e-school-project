import {SchoolRole} from "./User";

export const schoolId = 1
export const periodId = 1
export const users = [{
    id: 1,
    firstName: "John",
    middleName: "M",
    lastName: "Doe",
    username: "johndoe",
    role: SchoolRole.STUDENT,
    email: "john.doe@example.com"
},
    {
        id: 2,
        firstName: "Jane",
        middleName: "A",
        lastName: "Smith",
        username: "janesmith",
        role: SchoolRole.TEACHER,
        email: "jane.smith@example.com"
    },
    {
        id: 3,
        firstName: "PArent",
        middleName: "A",
        lastName: "PPP",
        username: "parent",
        role: SchoolRole.PARENT,
        email: "jane.smith@example.com"
    },
    {
        id: 4,
        firstName: "Anika",
        middleName: "P",
        lastName: "Kartselska",
        username: "anikakartselska",
        role: SchoolRole.ADMIN,
        email: "anika.kartselska@example.com"
    }]
export const currentUser = users[2]

