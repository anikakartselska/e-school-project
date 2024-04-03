package com.nevexis.backend.schoolManagement.security.reset_password

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.users.user_security.UserSecurity
import com.nevexis.backend.schoolManagement.users.user_security.UserSecurityService
import com.nevexis.`demo-project`.jooq.tables.records.PasswordResetTokenRecord
import com.nevexis.`demo-project`.jooq.tables.references.PASSWORD_RESET_TOKEN
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate


@Service
class PasswordResetService : BaseService() {
    @Autowired
    private lateinit var userSecurityService: UserSecurityService

//    fun resetPassword(
//        email: String
//    ): GenericResponse? {
//        val user: UserSecurity = userSecurityService.findUserByEmail(email) ?: throw SMSError(
//            "NOT_FOUND",
//            "User with email $email does not exist"
//        )
//        val token = UUID.randomUUID().toString()
//        createPasswordResetTokenForUser(user, token)
//        mailSender.send(
//            constructResetTokenEmail(
//                getAppUrl(request),
//                request.getLocale(), token, user
//            )
//        )
//        return GenericResponse(
//            messages.getMessage(
//                "message.resetPasswordEmail", null,
//                request.getLocale()
//            )
//        )
//    }

    fun createPasswordResetTokenForUser(user: UserSecurity?, token: String?): PasswordResetTokenRecord {
        val passwordResetToken = db.newRecord(PASSWORD_RESET_TOKEN).apply {
            id = getRequestSeqNextVal()
            this.token = token
            this.userId = user?.id?.toBigDecimal()
            expiryDate = LocalDate.now().plusDays(1)
        }

        return passwordResetToken
    }

    fun getRequestSeqNextVal(): BigDecimal =
        db.select(DSL.field("PASSWORD_RESET_TOKEN_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }
}