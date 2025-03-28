package com.nevexis.backend.schoolManagement.messages

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.actions.PaginatedFetchingInformationDTO
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.`demo-project`.jooq.tables.records.ChatRecord
import com.nevexis.`demo-project`.jooq.tables.records.MessageRecord
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.CHAT
import com.nevexis.`demo-project`.jooq.tables.references.CHAT_MEMBERS
import com.nevexis.`demo-project`.jooq.tables.references.MESSAGE
import com.nevexis.`demo-project`.jooq.tables.references.USER
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jooq.Record
import org.jooq.SelectOnConditionStep
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class ChatService : BaseService() {

    @Autowired
    private lateinit var userService: UserService

    fun getMessagesFromChat(chatId: BigDecimal): List<Message> {
        return db.select(MESSAGE.asterisk(), USER.asterisk())
            .from(MESSAGE)
            .leftJoin(USER)
            .on(USER.ID.eq(MESSAGE.USER_ID))
            .where(MESSAGE.CHAT_ID.eq(chatId))
            .orderBy(MESSAGE.SEND_ON.desc())
            .fetch()
            .map {
                val userRecord = it.into(UserRecord::class.java)
                it.into(MessageRecord::class.java).mapToInternalModel(userRecord)
            }
    }

    fun getLast10GroupChats(
        searchText: String,
        currentUserId: BigDecimal
    ): List<Chat> {
        return db.selectFrom(CHAT).where(CHAT.CHAT_TYPE.eq(ChatType.GROUP_CHAT.name))
            .and(
                CHAT.ID.`in`(
                    db.select(CHAT_MEMBERS.CHAT_ID)
                        .from(CHAT_MEMBERS)
                        .where(CHAT_MEMBERS.USER_ID.eq(currentUserId)).distinct()
                )
            ).and(
                DSL.upper(CHAT.CHAT_NAME).contains(searchText.replace(" ", "").uppercase())
            )
            .maxRows(10)
            .fetch().map { chatRecord ->
                Chat(
                    id = chatRecord.id!!,
                    chatName = chatRecord.chatName!!,
                    chatType = ChatType.valueOf(chatRecord.chatType!!)
                )
            }
    }


    fun fetchChatMessagesWithFiltersAndPagination(
        messagesFetchingInformationDTO: PaginatedFetchingInformationDTO,
        chatId: BigDecimal,
        currentUserId: BigDecimal
    ): List<Message> {
        val (startRange,
            endRange,
            _, _, _) = messagesFetchingInformationDTO
        val ROWNUM = DSL.field("ROWNUM", Int::class.java)
        val RN = DSL.field("RN", Int::class.java)
        val MESSAGES_TABLE = DSL.table("MESSAGES")

        val selectMessagesWithAppliedFiltersQuery = db.select(MESSAGE.asterisk())
            .from(MESSAGE)
            .where(MESSAGE.CHAT_ID.eq(chatId))
            .orderBy(MESSAGE.SEND_ON.desc()).asTable(MESSAGES_TABLE)

        // The following is done for fetching with range in oracle 11 with jooq due to lack of limit/offset functions.
        // for reference: https://www.jooq.org/doc/latest/manual/sql-building/sql-statements/select-statement/limit-clause
        val selectMessagesRownumWithEndRangeFilterQuery =
            db.select(selectMessagesWithAppliedFiltersQuery.asterisk(), ROWNUM.`as`(RN)).from(
                selectMessagesWithAppliedFiltersQuery
            ).where(ROWNUM.lessOrEqual(endRange)).asTable(MESSAGE)

        return db.select(selectMessagesRownumWithEndRangeFilterQuery.asterisk(), USER.asterisk()).from(
            selectMessagesRownumWithEndRangeFilterQuery
        ).leftJoin(USER)
            .on(selectMessagesRownumWithEndRangeFilterQuery.field("USER_ID", BigDecimal::class.java)?.eq(USER.ID))
            .where(RN.greaterThan(startRange))
            .orderBy(DSL.field("SEND_ON", LocalDateTime::class.java))
            .fetch().map {
                val userRecord = it.into(UserRecord::class.java)
                it.into(MessageRecord::class.java).mapToInternalModel(userRecord)
            }.also { fetchedMessages ->
                updateMessagesReadFromUser(
                    fetchedMessages.filter { !it.readFromUserIds.contains(currentUserId.toInt()) },
                    currentUserId
                )
            }
    }

    fun findDirectChatWithUser(userId: BigDecimal, currentUserId: BigDecimal): Pair<Chat, Message>? {
        val chatId = db.select(CHAT.ID.`as`("chat_id"))
            .from(CHAT)
            .join(CHAT_MEMBERS.`as`("CHAT_MEMBERS_1")).on(CHAT.ID.eq(CHAT_MEMBERS.`as`("CHAT_MEMBERS_1").CHAT_ID))
            .join(CHAT_MEMBERS.`as`("CHAT_MEMBERS_2")).on(CHAT.ID.eq(CHAT_MEMBERS.`as`("CHAT_MEMBERS_2").CHAT_ID))
            .where(CHAT_MEMBERS.`as`("CHAT_MEMBERS_1").USER_ID.eq(userId))
            .and(CHAT_MEMBERS.`as`("CHAT_MEMBERS_2").USER_ID.eq(currentUserId))
            .and(CHAT.CHAT_TYPE.eq(ChatType.DIRECT_MESSAGES.name))
            .fetchAnyInto(BigDecimal::class.java)

        return getChatAndLastMessageSelectConditionStep()
            .where(CHAT.ID.eq(chatId))
            .fetchAny()?.map {
                val userRecord = it.into(UserRecord::class.java).apply {
                    id = it.get(DSL.field("user_id", BigDecimal::class.java))
                }
                val chat = it.into(ChatRecord::class.java).apply {
                    id = it.get(DSL.field("chat_id", BigDecimal::class.java))
                }.mapToInternalModel()
                val lastMessageFromChat = it.into(MessageRecord::class.java).mapToInternalModel(userRecord)

                chat to lastMessageFromChat
            }
    }

    fun getLastChatsOfUserWithLastMessage(userId: BigDecimal): List<Pair<Chat, Message>> {
        val lastMessage = db.select(
            MESSAGE.ID,
            MESSAGE.CHAT_ID,
            MESSAGE.USER_ID,
            MESSAGE.CONTENT,
            MESSAGE.SEND_ON,
            MESSAGE.READ_FROM_USER_IDS // Ensure READ column is explicitly selected
        )
            .from(MESSAGE)
            .where(
                MESSAGE.ID.`in`(
                    db.select(DSL.max(MESSAGE.ID))
                        .from(MESSAGE)
                        .groupBy(MESSAGE.CHAT_ID)
                )
            ).asTable("last_message") // Alias the subquery

        return db.select(
            CHAT.asterisk().except(DSL.field("ID")),
            CHAT.ID.`as`("chat_id"),
            lastMessage.asterisk(),  // Use the properly aliased subquery
            USER.ID.`as`("user_id"),
            USER.asterisk().except(DSL.field("ID"))
        )
            .from(CHAT)
            .leftJoin(lastMessage).on(lastMessage.field("CHAT_ID", BigDecimal::class.java)?.eq(CHAT.ID))
            .leftJoin(USER).on(lastMessage.field("USER_ID", BigDecimal::class.java)?.eq(USER.ID))
            .leftJoin(CHAT_MEMBERS).on(CHAT_MEMBERS.CHAT_ID.eq(CHAT.ID))
            .where(CHAT_MEMBERS.USER_ID.eq(userId))
            .orderBy(lastMessage.field("SEND_ON")?.desc())
            .fetch()
            .map {
                val userRecord = it.into(UserRecord::class.java).apply {
                    id = it.get(DSL.field("user_id", BigDecimal::class.java))
                }
                val chat = it.into(ChatRecord::class.java).apply {
                    id = it.get(DSL.field("chat_id", BigDecimal::class.java))
                }.mapToInternalModel()
                val lastMessageFromChat = it.into(MessageRecord::class.java).mapToInternalModel(userRecord)

                chat to lastMessageFromChat
            }
    }

    fun getChatAndLastMessageSelectConditionStep(): SelectOnConditionStep<Record> {
        val lastMessage = db.select(
            MESSAGE.ID,
            MESSAGE.CHAT_ID,
            MESSAGE.USER_ID,
            MESSAGE.CONTENT,
            MESSAGE.SEND_ON,
            MESSAGE.READ_FROM_USER_IDS // Ensure READ column is explicitly selected
        )
            .from(MESSAGE)
            .where(
                MESSAGE.ID.`in`(
                    db.select(DSL.max(MESSAGE.ID))
                        .from(MESSAGE)
                        .groupBy(MESSAGE.CHAT_ID)
                )
            ).asTable("last_message") // Alias the subquery

        return db.select(
            CHAT.asterisk().except(DSL.field("ID")),
            CHAT.ID.`as`("chat_id"),
            lastMessage.asterisk(),  // Use the properly aliased subquery
            USER.ID.`as`("user_id"),
            USER.asterisk().except(DSL.field("ID"))
        )
            .from(CHAT)
            .leftJoin(lastMessage).on(lastMessage.field("CHAT_ID", BigDecimal::class.java)?.eq(CHAT.ID))
            .leftJoin(USER).on(lastMessage.field("USER_ID", BigDecimal::class.java)?.eq(USER.ID))
            .leftJoin(CHAT_MEMBERS).on(CHAT_MEMBERS.CHAT_ID.eq(CHAT.ID))
    }

    fun fetchMessagesWithFiltersAndPagination(
        messagesFetchingInformationDTO: PaginatedFetchingInformationDTO,
    ): List<Pair<Chat, Message>> {
        val (startRange,
            endRange,
            forUserId,
            _,
            _) = messagesFetchingInformationDTO
        val ROWNUM = DSL.field("ROWNUM", Int::class.java)
        val RN = DSL.field("RN", Int::class.java)
        val MESSAGES_CHAT_TABLE = DSL.table("MESSAGES_CHAT_TABLE")

        val lastMessage = db.select(
            MESSAGE.ID,
            MESSAGE.CHAT_ID,
            MESSAGE.USER_ID,
            MESSAGE.CONTENT,
            MESSAGE.SEND_ON,
            MESSAGE.READ_FROM_USER_IDS // Ensure READ column is explicitly selected
        )
            .from(MESSAGE)
            .where(
                MESSAGE.ID.`in`(
                    db.select(DSL.max(MESSAGE.ID))
                        .from(MESSAGE)
                        .groupBy(MESSAGE.CHAT_ID)
                )
            ).asTable("last_message") // Alias the subquery

        val selectMessagesWithAppliedFiltersQuery = db.select(
            CHAT.asterisk().except(DSL.field("ID")),
            CHAT.ID.`as`("chat_id"),
            lastMessage.asterisk(),  // Use the properly aliased subquery
            USER.ID.`as`("user_id"),
            USER.asterisk().except(DSL.field("ID"))
        )
            .from(CHAT)
            .leftJoin(lastMessage).on(lastMessage.field("CHAT_ID", BigDecimal::class.java)?.eq(CHAT.ID))
            .leftJoin(USER).on(lastMessage.field("USER_ID", BigDecimal::class.java)?.eq(USER.ID))
            .leftJoin(CHAT_MEMBERS).on(CHAT_MEMBERS.CHAT_ID.eq(CHAT.ID))
            .where(CHAT_MEMBERS.USER_ID.eq(forUserId))
            .orderBy(lastMessage.field("SEND_ON")?.desc())
            .asTable(MESSAGES_CHAT_TABLE)

        val selectMessagesRownumWithEndRangeFilterQuery =
            db.select(selectMessagesWithAppliedFiltersQuery.asterisk(), ROWNUM.`as`(RN)).from(
                selectMessagesWithAppliedFiltersQuery
            ).where(ROWNUM.lessOrEqual(endRange)).asTable()

        return db.select(selectMessagesRownumWithEndRangeFilterQuery.asterisk()).from(
            selectMessagesRownumWithEndRangeFilterQuery
        ).where(RN.greaterThan(startRange))
            .fetch()
            .map {
                val userRecord = it.into(UserRecord::class.java).apply {
                    id = it.get(selectMessagesRownumWithEndRangeFilterQuery.field("user_id", BigDecimal::class.java))
                }
                val chat = it.into(ChatRecord::class.java).apply {
                    id = it.get(selectMessagesRownumWithEndRangeFilterQuery.field("chat_id", BigDecimal::class.java))
                }.mapToInternalModel()
                val lastMessageFromChat = it.into(MessageRecord::class.java).mapToInternalModel(userRecord)

                chat to lastMessageFromChat
            }

    }


    fun insertMessage(message: Message) {
        db.newRecord(MESSAGE).apply {
            id = message.id
            userId = message.user.id.toBigDecimal()
            content = Json.encodeToString(message.content)
            sendOn = message.sendOn
            chatId = message.chatId
            readFromUserIds = Json.encodeToString(message.readFromUserIds)
        }.insert()
    }

    fun updateMessagesReadFromUser(messages: List<Message>, userId: BigDecimal) {
        if (messages.isNotEmpty()) {
            db.selectFrom(MESSAGE)
                .where(MESSAGE.ID.`in`(messages.map { it.id!! }))
                .fetch()
                .map { messageRecord ->
                    messageRecord.apply {
                        readFromUserIds =
                            Json.encodeToString((this.readFromUserIds?.let { Json.decodeFromString<List<Int>>(it) }
                                ?: emptyList()).plus(userId.toInt()).distinct())
                    }
                }.apply {
                    db.batchUpdate(this).execute()
                }
        }
    }

    fun createUpdateChat(chat: Chat): Chat {
        val chatId = chat.id ?: getChatSeqNextVal()

        (db.selectFrom(CHAT).where(CHAT.ID.eq(chatId)).fetchAny() ?: db.newRecord(CHAT)).apply {
            id = chatId
            chatType = chat.chatType.name
            chatName = chat.chatName
        }.store()

        val chatMemberIds = chat.chatMembers?.map { it.id.toBigDecimal() } ?: emptyList()
        val chatMembersFromDatabase = db.selectFrom(CHAT_MEMBERS).where(CHAT_MEMBERS.CHAT_ID.eq(chatId)).fetch()
        val chatMemberFromDatabaseIds = chatMembersFromDatabase.map { it.userId }

        val chatMembersToDelete =
            chatMembersFromDatabase.filter { chatMembersRecord ->
                !chatMemberIds.contains(chatMembersRecord.userId)
            }

        val chatMembersToAdd = chat.chatMembers?.filter {
            !chatMemberFromDatabaseIds.contains(it.id.toBigDecimal())
        }

        chatMembersToAdd?.map {
            db.newRecord(CHAT_MEMBERS)
                .apply {
                    this.chatId = chatId
                    this.userId = it.id.toBigDecimal()
                }
        }.apply {
            db.batchInsert(this).execute()
        }
        db.batchDelete(chatMembersToDelete).execute()

        return chat.copy(id = chatId)
    }

    fun ChatRecord.mapToInternalModel() = Chat(
        id = this.id,
        chatName = this.chatName!!,
        chatType = ChatType.valueOf(this.chatType!!),
        chatMembers = emptyList()
    )


    fun MessageRecord.mapToInternalModel(userRecord: UserRecord) =
        Message(
            id = this.id,
            user = userService.mapToUserView(userRecord, emptyList(), true),
            content = Json.decodeFromString(this.content!!),
            sendOn = this.sendOn!!,
            chatId = this.chatId!!,
            readFromUserIds = this.readFromUserIds?.let { Json.decodeFromString(it) } ?: emptyList()
        )

    fun getChatSeqNextVal(): BigDecimal =
        db.select(DSL.field("CHAT_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }
}