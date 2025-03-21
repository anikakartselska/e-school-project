import {UserView} from "./User";

export interface Message {

    id: number | null

    user: UserView,

    content: MessageContent,

    sendOn: Date | null,

    chatId: number,

    readFromUserIds: number[]

}


export interface MessageContent {

    text: string | null,

    files: FileWithBase64[] | null
}

export interface FileWithBase64 {
    name: string;
    base64: string;
}