import {UserView} from "./User";

export interface Actions {
    id: number,
    executedTime: Date | null,
    executedBy: UserView,
    periodId: number,
    schoolId: number,
    action: string
}


export interface ActionsFetchingInformationDTO {
    startRange: number,
    endRange: number,
    forUserId: number,
    periodId: number,
    schoolId: number,
}