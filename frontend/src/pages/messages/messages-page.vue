<template>
    <div class="row q-col-gutter-lg bg-sms">
        <div class="col-1"></div>
        <div class="col-10">
            <q-page class="page-content" padding>
                <div style="margin-top: 30px;">
                    <div class="text-h4 q-mb-md">Съобщения</div>
                    <q-separator/>
                    <div class="row">
                        <div class="col-3">
                            <q-select v-model="text"
                                      :option-label="user => user.firstName + ' ' + user.lastName + ' ('+ getUserRoles(user)+')'"
                                      :options="users" label="Потърсете чат" use-input
                                      @filter="filterFn"
                                      @update:model-value="value => checkExistingChat(value)">
                                <template v-slot:append>
                                    <q-icon name="search"/>
                                </template>
                                <template v-slot:before>
                                    <q-btn color="primary" icon="group_add" size="sm" square @click="groupCreateDialog">
                                        <q-tooltip>Създай група</q-tooltip>
                                    </q-btn>
                                </template>
                            </q-select>
                            <q-scroll-area style="height: 80vh">
                                <q-infinite-scroll ref="infiniteScroll" :offset="250" @load="onLoad">
                                    <q-list v-if="chatToMessages.length > 0" separator>
                                        <q-item v-for="chatToMess in chatToMessages" v-ripple
                                                :class="!chatToMess.second.readFromUserIds.includes(currentUser.id) && currentUser.id!==chatToMess.second.user.id
    ? `bg-blue-1`
    : (chatToMess.first.id === selectedChat.first.id ? `bg-blue-2` : ``)" clickable
                                                @click="selectChat(chatToMess)">
                                            <q-item-section avatar>
                                                <q-avatar v-if="chatToMess.second.user.profilePicture!=null"
                                                          text-color="white">
                                                    <q-img
                                                            :src="imageUrl(userIdToFile[chatToMess.second.user.id],chatToMess.second.user.id)"
                                                    ></q-img>
                                                </q-avatar>
                                                <q-avatar v-else color="cyan-2" text-color="white">
                                                    {{
                                                    chatToMess.second.user.firstName[0]
                                                    }}{{ chatToMess.second.user.lastName[0] }}
                                                </q-avatar>
                                            </q-item-section>
                                            <q-item-section>
                                                <q-item-label>
                                                    {{ chatToMess.first.chatName }}
                                                </q-item-label>
                                                <q-item-label caption>
                                                    <b>{{ chatToMess.second.user.firstName }}
                                                        {{ chatToMess.second.user.lastName }}:</b>
                                                    {{
                                                    chatToMess.second.content.text ? chatToMess.second.content.text : 'Изпрати прикачен файл'
                                                    }}
                                                </q-item-label>
                                            </q-item-section>
                                            <q-item-section side top>
                                                <q-item-label caption>
                                                    {{
                                                    dateTimeToBulgarianLocaleString(chatToMess.second.sendOn)
                                                    }}
                                                    <q-badge
                                                            v-if="!chatToMess.second.readFromUserIds.includes(currentUser.id) && currentUser.id!==chatToMess.second.user.id "
                                                            color="primary"
                                                            rounded/>
                                                    <br>
                                                    <q-btn v-if="chatToMess.first.chatType === ChatType.GROUP_CHAT"
                                                           class="float-right"
                                                           color="primary" dense flat icon="exit_to_app"
                                                           @click="leaveGroupChat(chatToMess)"></q-btn>
                                                    <q-btn v-if="chatToMess.first.chatType === ChatType.GROUP_CHAT"
                                                           class="float-right"
                                                           color="primary" dense flat icon="settings"
                                                           @click="editChat(chatToMess)"></q-btn>
                                                </q-item-label>
                                                <q-icon color="primary" name="icon"/>
                                            </q-item-section>
                                        </q-item>
                                    </q-list>
                                    <h6 v-else style="text-align: center">Няма известия
                                        <q-icon name="sentiment_dissatisfied"/>
                                    </h6>
                                    <template v-slot:loading>
                                        <div class="row justify-center q-my-md">
                                            <q-spinner-dots color="primary" size="40px"/>
                                        </div>
                                    </template>
                                </q-infinite-scroll>
                            </q-scroll-area>
                        </div>
                        <q-separator vertical></q-separator>
                        <div class="col q-ml-xl">
                            <div style="width: 100%; max-width: 900px;height: 70vh">
                                <q-scroll-area ref="messagesScroll" style="height: 70vh">
                                    <q-infinite-scroll v-if="selectedChat" ref="messagesInfiniteScroll"
                                                       :offset="250" reverse
                                                       @load="onMessagesLoad">
                                        <q-chat-message
                                                v-for="message in messages"
                                                v-if="messages.length > 0"
                                                :key="message.id"
                                                :name="message.user.firstName"
                                                :sent="currentUser.id === message.user.id"
                                                :stamp="dateTimeToBulgarianLocaleString(message.sendOn)"
                                                bg-color="amber-7">
                                            <template v-slot:avatar>
                                                <q-avatar v-if="message.user.profilePicture != null" text-color="white">
                                                    <q-img :src="imageUrl(userIdToFile[message.user.id], message.user.id)"></q-img>
                                                </q-avatar>
                                                <q-avatar v-else color="cyan-2" text-color="white">
                                                    {{ message.user.firstName[0] }}{{ message.user.lastName[0] }}
                                                </q-avatar>
                                            </template>
                                            <template v-if="message.content.files?.length > 0">
                                                <div v-for="file in message.content.files">
                                                    <div v-if="!isBase64Image(file.base64)">
                                                        <q-btn :key="file"
                                                               color="primary"
                                                               icon="attach_file"
                                                               label="Изтегли файл"
                                                               target="_blank"
                                                               @click="downloadFile(dataURLtoFile(file.base64,file.name))"
                                                        >
                                                        </q-btn>
                                                    </div>
                                                    <div v-else>
                                                        <q-img :src="imageUrlForMessage(base64ToImageFile(file.base64,file.name), message.id)"></q-img>
                                                    </div>
                                                </div>

                                            </template>
                                            <template v-else>
                                                <div>
                                                    {{ message.content.text }}
                                                </div>
                                            </template>
                                        </q-chat-message>
                                        <span v-if="messages.length === 0" class="text-primary">
                          Все още не сте започнали разговор с {{ selectedChat.first.chatName }}. Изпратете първото си съобщение и започнете комуникацията!
                    </span>
                                    </q-infinite-scroll>
                                </q-scroll-area>
                            </div>
                            <div class="row">
                                <q-input v-model="newMessage.text" autogrow class="col-11"
                                         label="Напиши съобщение"></q-input>
                                <q-btn class="col" color="primary" dense flat icon="send" rounded
                                       :disable="!newMessage.text && files.length === 0 " @click="sendMessage"></q-btn>
                            </div>
                            <div class="row">
                                <q-file v-model="files" class="col-12" multiple outlined square use-chips>
                                    <template v-slot:prepend>
                                        <q-icon name="attach_file"/>
                                    </template>
                                </q-file>
                            </div>
                        </div>
                    </div>
                </div>
            </q-page>
        </div>
    </div>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";
import {useRouter} from "vue-router";
import {QInfiniteScroll, QScrollArea, useQuasar} from "quasar";
import {PaginatedFetchingInformationDTO} from "../../model/Actions";
import {getCurrentUser, getCurrentUserAsUserView} from "../../services/LocalStorageService";
import {
    fetchDirectChatWithUser,
    get10UserViewsBySchoolMatchingSearchText,
    getChatMembers,
    getChatMessagesWithFiltersAndPagination,
    getLast10GroupChats,
    getMessagesWithFiltersAndPagination,
    saveUpdateChat,
    sendCreateMessage
} from "../../services/RequestService";
import {Chat, ChatType} from "../../model/Chat";
import {FileWithBase64, Message, MessageContent} from "../../model/Message";
import {Pair} from "../../model/Pair";
import {
    confirmActionPromiseDialog,
    dataURLtoFile,
    dateTimeToBulgarianLocaleString,
    downloadFile,
    fileToBase64,
    isBase64Image,
    translationOfRoles
} from "../../utils";
import {onBeforeMount, onBeforeUnmount, watch} from "vue";
import {chatMessagesEventSource, setupChatMessageEventSource} from "../../services/ChatMessagesService";
import {UserView} from "../../model/User";
import GroupChatCreateEditDialog from "./group-chat-create-edit-dialog.vue";
import {periodId, schoolId} from "../../model/constants";


const props = defineProps<{
    periodId: number
    schoolId: number
}>()

const router = useRouter()

let infiniteScroll = $ref<InstanceType<typeof QInfiniteScroll>>()
let messagesInfiniteScroll = $ref<InstanceType<typeof QInfiniteScroll>>()
let messagesScroll = $ref<InstanceType<typeof QScrollArea>>()
const currentUser = getCurrentUser()
let newMessage = $ref<MessageContent>({text: null, files: null})
let files = $ref<File[]>([])
const LOADED_ROWS_COUNT = 20
const LOADED_MESSAGES_COUNT = 20
const text = $ref<UserView | null>(null)
let users = $ref<UserView[]>([])
let chats = $ref<Chat[]>([])
let userIdToFile = $ref({})
let messageIdToFiles = $ref({})
let selectedChat = $ref<Pair<Chat, Message | null> | null>(null)
let messages = $ref<Message[]>([])

const filterFn = (val, update) => {
    if (val === '') {
        update(() => {
            users = []

            // here you have access to "ref" which
            // is the Vue reference of the QSelect
        })
        return
    }

    update(async () => {
        users = await get10UserViewsBySchoolMatchingSearchText(props.schoolId, props.periodId, val)
        chats = await getLast10GroupChats(val)
    })
}
const getAllChatWithPaginationAndFilters = async (loadingIndex): Promise<Pair<Chat, Message>[]> => {
    const messagesFetchingInformationDTO: PaginatedFetchingInformationDTO = {
        startRange: (loadingIndex * LOADED_ROWS_COUNT) - LOADED_ROWS_COUNT,
        endRange: loadingIndex * LOADED_ROWS_COUNT,
        forUserId: currentUser.id,
        periodId: +props.periodId,
        schoolId: +props.schoolId,
    }
    return await getMessagesWithFiltersAndPagination(messagesFetchingInformationDTO)

}


const groupCreateDialog = () => {
    quasar.dialog({
        component: GroupChatCreateEditDialog,
        componentProps: {}
    }).onOk(async (payload) => {
        debugger
        await saveUpdateChat(payload.item.chat).then(r => {
            selectedChat = {...selectedChat, first: r}
        })
    })
}

const editChat = async (chatToMessage: Pair<Chat, Message>) => {
    quasar.dialog({
        component: GroupChatCreateEditDialog,
        componentProps: {
            chat: {
                ...chatToMessage.first,
                chatMembers: await getChatMembers(chatToMessage.first.id, schoolId.value, periodId.value)
            }
        }
    }).onOk(async (payload) => {
        debugger
        await saveUpdateChat(payload.item.chat).then(r => {
            selectedChat = {...selectedChat, first: r}
            chatToMessages = chatToMessages.map(chatToMess => {
                        if (chatToMess.first.id === chatToMessage.first.id) {
                            return selectedChat
                        } else {
                            return chatToMess
                        }
                    }
            )
        })
    })
}

const leaveGroupChat = async (chatToMessage: Pair<Chat, Message>) => {
    await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
    let members = await getChatMembers(chatToMessage.first.id, schoolId.value, periodId.value)
    members = members.filter(member => member.id !== currentUser.id)
    await saveUpdateChat({...chatToMessage.first, chatMembers: members}).then(r => {

        chatToMessages = chatToMessages.filter(chatToMess =>
                chatToMess.first.id !== chatToMessage.first.id
        )
        selectedChat = chatToMessages[0]
    })

}
const sendMessage = async () => {
    if (selectedChat?.first.id == null) {
        await saveUpdateChat(selectedChat?.first!!).then(r => {
            selectedChat = {...selectedChat, first: r}
        })
    }
    const base64Files = await Promise.all(files.map(file => fileToBase64(file)));

    const messageContentWithFiles: MessageContent = {
        ...newMessage,
        files: files.map((file, ind) => <FileWithBase64>{name: file.name, base64: base64Files[ind]}), // Ensure this contains resolved Base64 strings, not promises
    };
    let message = <Message>{
        id: null,
        user: chatMembers.find(it => it.id == currentUser.id)!!,
        content: messageContentWithFiles,
        sendOn: new Date(),
        chatId: selectedChat?.first.id,
        readFromUserIds: []
    }
    await sendCreateMessage(message).then(mess =>
            message = mess
    )
    const chatToMessagesIndex = chatToMessages.findIndex(chatToMessages => chatToMessages.first.id == selectedChat?.first.id)
    if (chatToMessagesIndex != -1) {
        chatToMessages.splice(chatToMessagesIndex, 1)
    }
    chatToMessages.unshift({...selectedChat, second: message})
    newMessage = {text: null, files: null}
    files = []
}

const checkExistingChat = async (userView: UserView) => {
    await fetchDirectChatWithUser(userView.id).then(r => {
                if (r) {
                    selectedChat = r
                } else {
                    const newChat = <Chat>{
                        chatName: userView.firstName + ' ' + userView.lastName,
                        chatType: ChatType.DIRECT_MESSAGES,
                        chatMembers: [userView, getCurrentUserAsUserView()]
                    }
                    selectedChat = <Pair<Chat, Message | null>>{first: newChat, second: null}
                }
            }
    )
}
const getAllMessagesWithPaginationAndFilters = async (loadingIndex): Promise<Message[]> => {
    const messagesFetchingInformationDTO: PaginatedFetchingInformationDTO = {
        startRange: (loadingIndex * LOADED_MESSAGES_COUNT) - LOADED_MESSAGES_COUNT,
        endRange: loadingIndex * LOADED_MESSAGES_COUNT,
        forUserId: currentUser.id,
        periodId: props.periodId,
        schoolId: props.schoolId,
    }
    return selectedChat?.first.id ? await getChatMessagesWithFiltersAndPagination(messagesFetchingInformationDTO, selectedChat?.first.id) : []

}
let chatToMessages = $ref<Pair<Chat, Message>[]>([])

const onLoad = async (index: number, done: () => void) => {
    const fetched = await getAllChatWithPaginationAndFilters(index)
    if (fetched.length < LOADED_ROWS_COUNT) {
        infiniteScroll.stop()
    }
    chatToMessages = chatToMessages.concat(fetched)
    done()
}

watch(() => chatToMessages, (curr, prior) => {
            if (chatToMessages.length > 0 && prior.length == 0) {
                selectedChat = chatToMessages[0]
            }
        }
)

const onMessagesLoad = async (index: number, done: () => void) => {
    const fetched = await getAllMessagesWithPaginationAndFilters(index)
    if (fetched.length < LOADED_MESSAGES_COUNT) {
        messagesInfiniteScroll.stop()
    }
    messages = fetched.concat(messages)
    fetched.forEach(fetch => {
                messageIdToFiles[fetch.id!!] = fetch.content.files?.map(file =>
                        base64ToImageFile(file.base64, file.name)
                )
            }
    )
    done()
}
let chatMembers = $ref<UserView[]>([])

watch(() => selectedChat?.first, async () => {
    if (selectedChat) {
        userIdToFile = {}
        chatMembers = await getChatMembers(selectedChat.first.id, schoolId.value, periodId.value)
        chatMembers?.forEach(member =>
                userIdToFile[member.id] = member.profilePicture ? base64ToImageFile(member.profilePicture, member.id!!.toString()) : null
        )
        console.log(userIdToFile)
        messages = []
        messagesInfiniteScroll?.reset()
        messagesInfiniteScroll?.resume()
        messagesScroll.setScrollPercentage("vertical", 100)
        chatToMessages = chatToMessages.map(chatToMess => {
                    if (selectedChat.first.id == chatToMess.first.id) {
                        return {
                            ...selectedChat, second: {
                                ...selectedChat?.second,
                                readFromUserIds: selectedChat?.second?.readFromUserIds.concat(currentUser.id)
                            }
                        }
                    } else {
                        return chatToMess
                    }
                }
        )
    }

})

const selectChat = (chatToMess: Pair<Chat, Message>) => {
    selectedChat = chatToMess
}
const quasar = useQuasar()

const cachedImageUrls = new Map<string, string>();
const imageUrl = (file: File, uuid: string) => {
    if (!file) return '';
    if (!cachedImageUrls.has(uuid)) {
        cachedImageUrls.set(uuid, window.URL.createObjectURL(file));
    }
    return cachedImageUrls.get(uuid);
};

const cachedMessageFiles = new Map<string, Pair<string, string>[]>();
const imageUrlForMessage = (file: File | null, uuid: string) => {
    if (!file || !(file instanceof File)) {
        console.error("Invalid file object", file);
        return '';
    }

    if (!cachedMessageFiles.has(uuid)) {
        cachedMessageFiles.set(uuid, [<Pair<string, string>>{
            first: window.URL.createObjectURL(file),
            second: file.name
        }]);
    } else {
        cachedMessageFiles.set(uuid, [...cachedMessageFiles.get(uuid)!, <Pair<string, string>>{
            first: window.URL.createObjectURL(file),
            second: file.name
        }]);
    }
    return cachedMessageFiles.get(uuid)?.find(url => url.second.includes(file.name))?.first;
};

function base64ToImageFile(base64String: string, fileName: string): File {
    const arr = base64String.split(",");
    const mimeType = arr[0].match(/:(.*?);/)?.[1] || "image/png";
    const byteCharacters = atob(arr[1]); // Decode Base64
    const byteNumbers = new Uint8Array(byteCharacters.length);


    for (let i = 0; i < byteCharacters.length; i++) {
        byteNumbers[i] = byteCharacters.charCodeAt(i);
    }

    const blob = new Blob([byteNumbers], {type: mimeType});
    return new File([blob], fileName, {type: mimeType});
}

onBeforeMount(async () => {
    setupChatMessageEventSource()
    chatMessagesEventSource.addEventListener('message', (actionMessage: MessageEvent) => {
        const newMessage2 = <Message>JSON.parse(actionMessage.data)
        messages.push(newMessage2)
    }, false)
})

onBeforeUnmount(() => {
    chatMessagesEventSource.close();
})

const getUserRoles = (userView: UserView) => {
    if (userView.roles.length == 0) {
        return "НЯМА АКТИВНИ РОЛИ"
    } else {
        return [...new Set(userView.roles.map(it => translationOfRoles[it]))].join(",")
    }
}
</script>
<style scoped>
.page-content {
    box-shadow: 0 30px 25px rgba(0, 0, 0, 0.4);
}

.menu {
    background-color: rgba(46, 185, 246, 0.27);
    color: black;
}

.scroll-area-for-find-by {
    height: 25vh;
}

.find-by-section {
    max-width: 36vh;
    margin-left: 7vh
}

q-toolbar {
    width: 36vh;
}

</style>
