import {UserView} from "./User";


export interface SmsFile {
    id: number | null,
    fileName: string,
    createdBy: UserView,
    note: string | null,
    createdOn: Date | null
}


