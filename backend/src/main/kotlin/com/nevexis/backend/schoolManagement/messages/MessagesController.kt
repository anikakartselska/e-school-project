package com.nevexis.backend.schoolManagement.messages

import com.nevexis.backend.schoolManagement.actions.PaginatedFetchingInformationDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.math.BigDecimal
import java.security.Principal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api/messages")
class MessagesController {
    @Autowired
    @Qualifier("messages-main-sink")
    private lateinit var sink: Sinks.Many<Message>

    @Autowired
    private lateinit var messagesService: MessagesService

    @Autowired
    private lateinit var chatService: ChatService

    @RequestMapping(path = ["/subscribe-messages"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun sse(): Flux<ServerSentEvent<Message>> = sink.asFlux().map { e: Message ->
        ServerSentEvent.builder(e).id(e.id.toString()).build()
    }

    @PostMapping("/send-message")
    fun sendMessage(
        @RequestBody message: Message
    ) = messagesService.createMessage(message)

    @GetMapping("get-last-chats-of-user-with-last-message")
    fun getLastChats(principal: Principal) =
        chatService.getLastChatsOfUserWithLastMessage(principal.name.toBigDecimal())

    @GetMapping("/get-messages-from-chat")
    fun fetchMessagesFromChat(chatId: BigDecimal) =
        chatService.getMessagesFromChat(chatId)

    @GetMapping("/get-last-10-group-chats")
    fun last10Chats(searchText: String, principal: Principal) =
        chatService.getLast10GroupChats(searchText, principal.name.toBigDecimal())


    @PostMapping("/get-messages-with-filters-and-pagination")
    fun getMessagesWithFiltersAndPagination(
        @RequestBody messagesFetchingInformationDTO: PaginatedFetchingInformationDTO,
    ) = chatService.fetchMessagesWithFiltersAndPagination(messagesFetchingInformationDTO)


    @PostMapping("/get-chat-messages-with-filters-and-pagination")
    fun getChatMessagesWithFiltersAndPagination(
        @RequestBody messagesFetchingInformationDTO: PaginatedFetchingInformationDTO,
        chatId: BigDecimal,
        principal: Principal
    ) = chatService.fetchChatMessagesWithFiltersAndPagination(
        messagesFetchingInformationDTO,
        chatId,
        principal.name.toBigDecimal()
    )

    @GetMapping("/get-chat-with-user")
    fun getChatWithUser(userId: BigDecimal, principal: Principal) =
        chatService.findDirectChatWithUser(userId, principal.name.toBigDecimal())

    @PostMapping("/save-update-chat")
    fun mergeChat(@RequestBody chat: Chat) = chatService.createUpdateChat(chat)

}