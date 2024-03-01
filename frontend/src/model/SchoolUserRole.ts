import {DetailsForParent, DetailsForStudent, DetailsForUser, SchoolRole} from "./User";
import {RequestStatus} from "./RequestStatus";
import {School} from "./School";
import {translationOfRoles} from "../utils";

export interface SchoolUserRole {
    id: number,
    userId: number,
    school: School,
    role: SchoolRole,
    status: RequestStatus
    detailsForUser: DetailsForUser | null
}

export const constructSchoolUserRoleMessage = (schoolUserRole: SchoolUserRole) => {
    let detailsMessage = ''
    switch (schoolUserRole.role) {
        case SchoolRole.ADMIN:
        case SchoolRole.TEACHER:
            break;
        case SchoolRole.PARENT: {
            const child = (schoolUserRole?.detailsForUser as DetailsForParent)?.child
            detailsMessage = ` - на ${child?.firstName} ${child?.lastName} от ${(child?.role.detailsForUser as DetailsForStudent).schoolClass?.name}`
            break;
        }
        case SchoolRole.STUDENT: {
            detailsMessage = ` - ученик в ${(schoolUserRole.detailsForUser as DetailsForStudent).schoolClass?.name}`
            break
        }
    }
    return `${translationOfRoles[schoolUserRole.role]} ${detailsMessage} `
}