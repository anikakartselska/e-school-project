package com.nevexis.backend.schoolManagement.security.reset_password

import com.nevexis.backend.beans.ServerUtils
import com.nevexis.backend.error_handling.SMSError
import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.email.NotificationService
import com.nevexis.backend.schoolManagement.email.TemplateType
import com.nevexis.backend.schoolManagement.users.user_security.UserSecurity
import com.nevexis.backend.schoolManagement.users.user_security.UserSecurityService
import com.nevexis.`demo-project`.jooq.tables.records.PasswordResetTokenRecord
import com.nevexis.`demo-project`.jooq.tables.references.PASSWORD_RESET_TOKEN
import com.nevexis.`demo-project`.jooq.tables.references.USER
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*


@Service
class PasswordResetService : BaseService() {
    @Autowired
    private lateinit var userSecurityService: UserSecurityService

    @Autowired private lateinit var serverUtils: ServerUtils

    @Autowired
    private lateinit var notificationService: NotificationService

    @Autowired
    @Lazy
    lateinit var passwordEncoder: PasswordEncoder

    fun resetPassword(
        email: String
    ): Boolean {
        val user: UserSecurity = userSecurityService.findUserByEmail(email) ?: throw SMSError(
            "Данните не са намерени",
            "Не съществува потребител с имейл:$email"
        )
        val token = UUID.randomUUID().toString()
        val passwordResetToken = createPasswordResetTokenForUser(user, token)

        return notificationService.sendNotification(
            listOf(user.email),
            "Смяна на парола в Е-училище",
            TemplateType.RESET_PASSWORD,
            getContext(passwordResetToken)
        )
    }

    private fun getContext(passwordResetToken: PasswordResetToken): Map<String, String> {
        return mapOf(
            "url" to "${serverUtils.getServerUrlWithoutProtocol()}/new-password/${passwordResetToken.token}",
        )
    }

    fun createPasswordResetTokenForUser(user: UserSecurity?, token: String?): PasswordResetToken {
        val passwordResetToken = db.newRecord(PASSWORD_RESET_TOKEN).apply {
            id = getRequestSeqNextVal()
            this.token = token
            this.userId = user?.id?.toBigDecimal()
            expiryDate = LocalDateTime.now().plusDays(1)
        }
        passwordResetToken.insert()

        return passwordResetToken.into(PasswordResetTokenRecord::class.java).map {
            PasswordResetToken(
                id = (it as PasswordResetTokenRecord).id!!.toInt(),
                token = it.token!!,
                user = user!!,
                expiryDate = it.expiryDate!!
            )
        }
    }

    fun updateUserPassword(newPassword: String, passwordResetToken: String) {
        val passwordResetTokenRecord =
            db.selectFrom(PASSWORD_RESET_TOKEN).where(PASSWORD_RESET_TOKEN.TOKEN.eq(passwordResetToken))
                .fetchAny()
        if (passwordResetTokenRecord?.expiryDate?.isBefore(LocalDateTime.now()) == true) {
            throw SMSError("Грешка при промяна на паролата", "Токенът за промяна на паролата е изтекъл")
        }
        db.selectFrom(USER).where(USER.ID.eq(passwordResetTokenRecord?.userId!!))
            .fetchAny()?.apply {
                password = passwordEncoder.encode(newPassword)
            }?.update()
    }

    fun getRequestSeqNextVal(): BigDecimal =
        db.select(DSL.field("PASSWORD_RESET_TOKEN_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }
}