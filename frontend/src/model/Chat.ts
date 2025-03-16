import {UserView} from "./User";

export interface Chat {
    id: number
    chatName: string,
    chatType: ChatType,
    chatMembers: UserView[] | null
}

export enum ChatType {
    DIRECT_MESSAGES = "DIRECT_MESSAGES",
    GROUP_CHAT = "GROUP_CHAT"
}