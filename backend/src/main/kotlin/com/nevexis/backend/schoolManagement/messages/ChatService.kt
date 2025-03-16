package com.nevexis.backend.schoolManagement.messages

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.actions.ActionsFetchingInformationDTO
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

    fun fetchChatMessagesWithFiltersAndPagination(
        messagesFetchingInformationDTO: ActionsFetchingInformationDTO,
        chatId: BigDecimal
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
            }
    }

    fun getLastChatsOfUserWithLastMessage(userId: BigDecimal): List<Pair<Chat, Message>> {

        val lastMessage = db.select(
            MESSAGE.ID,
            MESSAGE.CHAT_ID,
            MESSAGE.USER_ID,
            MESSAGE.CONTENT,
            MESSAGE.SEND_ON,
            MESSAGE.READ // Ensure READ column is explicitly selected
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

    fun fetchMessagesWithFiltersAndPagination(
        messagesFetchingInformationDTO: ActionsFetchingInformationDTO,
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
            MESSAGE.READ // Ensure READ column is explicitly selected
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
            read = if (message.read) {
                "Y"
            } else {
                "N"
            }
        }.insert()
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
            user = userService.mapToUserView(userRecord, emptyList()),
            content = Json.decodeFromString(this.content!!),
            sendOn = this.sendOn!!,
            chatId = this.chatId!!,
            read = this.read == "Y"
        )

}