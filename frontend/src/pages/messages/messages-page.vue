<template>
    <div class="row q-col-gutter-lg">
        <div class="col-1"></div>
        <div class="col-10">
            <q-page class="page-content" padding>
                <div style="margin-top: 30px;">
                    <div class="text-h4 q-mb-md">Съобщения</div>
                    <q-separator/>
                    <div class="row">
                        <div class="col-3">
                            <q-input v-model="text" label="Потърсете чат">
                                <template v-slot:append>
                                    <q-icon v-if="text === ''" name="search"/>
                                    <q-icon v-else class="cursor-pointer" name="clear" @click="text = ''"/>
                                </template>
                            </q-input>
                            <q-scroll-area style="height: 80vh">
                                <q-infinite-scroll ref="infiniteScroll" :offset="250" @load="onLoad">
                                    <q-list v-if="chatToMessages.length > 0" separator>
                                        <q-item v-for="chatToMess in chatToMessages" v-ripple
                                                clickable @click="selectChat(chatToMess)">
                                            <q-item-section>
                                                <q-item-label>
                                                    {{ chatToMess.first.chatName }}
                                                </q-item-label>
                                                <q-item-label caption>
                                                    <b>{{ chatToMess.second.user.firstName }}
                                                        {{ chatToMess.second.user.lastName }}:</b>
                                                    {{ chatToMess.second.content.text }}
                                                </q-item-label>
                                            </q-item-section>
                                            <q-item-section side top>
                                                <q-item-label caption>
                                                    {{
                                                    dateTimeToBulgarianLocaleString(chatToMess.second.sendOn)
                                                    }}
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
                                        <q-chat-message v-for="message in messages"
                                                        :name="message.user.firstName"
                                                        :sent="currentUser.id === message.user.id"
                                                        :stamp="[message.sendOn]"
                                                        :text="[message.content.text]"
                                                        avatar="https://cdn.quasar.dev/img/avatar3.jpg"
                                                        bg-color="amber-7"
                                        />
                                    </q-infinite-scroll>
                                </q-scroll-area>
                            </div>
                            <div class="row">
                                <q-input v-model="newMessage.text" autogrow class="col-11"
                                         label="Напиши съобщение"></q-input>
                                <q-btn class="col" color="primary" dense flat icon="photo_library" rounded></q-btn>
                                <q-btn class="col" color="primary" dense flat icon="send" rounded
                                       @click="sendMessage"></q-btn>
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
import {ActionsFetchingInformationDTO} from "../../model/Actions";
import {getCurrentUser, getCurrentUserAsUserView} from "../../services/LocalStorageService";
import {
    getChatMessagesWithFiltersAndPagination,
    getMessagesWithFiltersAndPagination,
    sendCreateMessage
} from "../../services/RequestService";
import {Chat} from "../../model/Chat";
import {Message, MessageContent} from "../../model/Message";
import {Pair} from "../../model/Pair";
import {dateTimeToBulgarianLocaleString} from "../../utils";
import {onBeforeMount, onBeforeUnmount, watch} from "vue";
import {chatMessagesEventSource, setupChatMessageEventSource} from "../../services/ChatMessagesService";


const props = defineProps<{
    periodId: number
    schoolId: number
}>()

const router = useRouter()

let infiniteScroll = $ref<InstanceType<typeof QInfiniteScroll>>()
let messagesInfiniteScroll = $ref<InstanceType<typeof QInfiniteScroll>>()
let messagesScroll = $ref<InstanceType<typeof QScrollArea>>()
const currentUser = getCurrentUser()
const newMessage = $ref<MessageContent>({text: null, picture: null})
const LOADED_ROWS_COUNT = 20
const LOADED_MESSAGES_COUNT = 20
const text = $ref("")

const getAllChatWithPaginationAndFilters = async (loadingIndex): Promise<Pair<Chat, Message>[]> => {
    const messagesFetchingInformationDTO: ActionsFetchingInformationDTO = {
        startRange: (loadingIndex * LOADED_ROWS_COUNT) - LOADED_ROWS_COUNT,
        endRange: loadingIndex * LOADED_ROWS_COUNT,
        forUserId: currentUser.id,
        periodId: props.periodId,
        schoolId: props.schoolId,
    }
    return await getMessagesWithFiltersAndPagination(messagesFetchingInformationDTO)

}

const sendMessage = async () => {
    const message = <Message>{
        user: getCurrentUserAsUserView(),

        content: newMessage,

        sendOn: new Date(),

        chatId: selectedChat?.first.id,

        read: false
    }
    await sendCreateMessage(message)
}

const getAllMessagesWithPaginationAndFilters = async (loadingIndex): Promise<Message[]> => {
    const messagesFetchingInformationDTO: ActionsFetchingInformationDTO = {
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

const onMessagesLoad = async (index: number, done: () => void) => {
    const fetched = await getAllMessagesWithPaginationAndFilters(index)
    if (fetched.length < LOADED_MESSAGES_COUNT) {
        messagesInfiniteScroll.stop()
    }
    messages = fetched.concat(messages)
    done()
}


let selectedChat = $ref<Pair<Chat, Message> | null>(null)
let messages = $ref<Message[]>([])
watch(() => selectedChat, async () => {
    if (selectedChat) {
        messages = []
        messagesScroll.setScrollPercentage("vertical", 100)
        messagesInfiniteScroll.reset()
        messagesInfiniteScroll.resume()
    }

})

const selectChat = (chatToMess: Pair<Chat, Message>) => {
    console.log("test")
    console.log(chatToMess)
    selectedChat = chatToMess
}
const quasar = useQuasar()
// onBeforeMount(async () => {
//   messagesEventSource.addEventListener('message', (actionMessage: MessageEvent) => {
//     const newMessage = <Message>JSON.parse(actionMessage.data)
//     console.log("hckzmkdc")
//     if (newMessage.chatId == selectedChat?.first.id) {
//       messages.push(newMessage)
//     }
//     // }
//   }, false)
// })

onBeforeMount(async () => {
    setupChatMessageEventSource()
    chatMessagesEventSource.addEventListener('message', (actionMessage: MessageEvent) => {
        const newMessage = <Message>JSON.parse(actionMessage.data)
        messages.push(newMessage)
    }, false)
    console.log(chatMessagesEventSource)
})
watch(() => messages, () => {
    messagesScroll.setScrollPercentage("vertical", 100)
})

onBeforeUnmount(() => {
    chatMessagesEventSource.close();
})

// function formatDateToBulgarian(date: Date | string): string {
//   const bgFormatter = new Intl.DateTimeFormat("bg-BG", {day: "2-digit", month: "long"});
//
//   const validDate = new Date(date);
//   if (isNaN(validDate.getTime())) return "Invalid Date"; // Handle invalid dates
//
//   console.log(validDate.getDate())
//   console.log(currentDate.getDate())
//   return !(validDate.getDate() == currentDate.getDate() && validDate.getMonth() == currentDate.getMonth() && validDate.getFullYear() == currentDate.getFullYear()) ?
//           bgFormatter.format(validDate) : 'Днес';
// }
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
