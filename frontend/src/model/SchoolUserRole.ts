import {DetailsForUser, SchoolRole} from "./User";
import {RequestStatus} from "./RequestStatus";
import {School} from "./School";

export interface SchoolUserRole {
    id: number,
    userId: number,
    school: School,
    role: SchoolRole,
    status: RequestStatus
    detailsForUser: DetailsForUser | null
}