import {DetailsForParent, DetailsForStudent, DetailsForTeacher, DetailsForUser, SchoolRole} from "./User";
import {RequestStatus} from "./RequestStatus";
import {School} from "./School";
import {translationOfRoles} from "../utils";
import {SchoolPeriod} from "./SchoolPeriod";

export interface SchoolUserRole {
    id: number | null,
    userId: number,
    period: SchoolPeriod,
    school: School,
    role: SchoolRole,
    status: RequestStatus
    detailsForUser: DetailsForUser | null
}

export const constructSchoolUserRoleMessageWithSchoolAndPeriodInformation = (schoolUserRole: SchoolUserRole) => {
    const detailsMessage = constructDetailsMessage(schoolUserRole)
    return `${translationOfRoles[schoolUserRole.role]} ${detailsMessage} - ${schoolUserRole.school.schoolName} - ${schoolUserRole.period.startYear.substring(0, 4)}/${schoolUserRole.period.endYear.substring(0, 4)}`
}
export const constructSchoolUserRoleMessage = (schoolUserRole: SchoolUserRole) => {
    const detailsMessage = constructDetailsMessage(schoolUserRole)
    return `${translationOfRoles[schoolUserRole.role]} ${detailsMessage} `
}
export const constructDetailsMessage = (schoolUserRole: SchoolUserRole) => {
    let detailsMessage = ''
    switch (schoolUserRole.role) {
        case SchoolRole.ADMIN: {
            detailsMessage = ''
            break;
        }
        case SchoolRole.TEACHER: {
            detailsMessage = ` - по ${(schoolUserRole.detailsForUser as DetailsForTeacher).subjects.join(",")}`
            break;
        }
        case SchoolRole.PARENT: {
            const child = (schoolUserRole?.detailsForUser as DetailsForParent)?.child
            detailsMessage = ` - на ${child?.firstName} ${child?.lastName} от ${(child?.role.detailsForUser as DetailsForStudent)?.schoolClass?.name}`
            break;
        }
        case SchoolRole.STUDENT: {
            detailsMessage = ` - в ${(schoolUserRole.detailsForUser as DetailsForStudent).schoolClass?.name}`
            break
        }
    }
    return detailsMessage
}