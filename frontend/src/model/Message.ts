import {UserView} from "./User";

export interface Message {

    id: number

    user: UserView,

    content: MessageContent,

    sendOn: Date | null,

    chatId: number,

    read: boolean

}


export interface MessageContent {

    text: string | null,

    picture: string | null
}