package com.nevexis.backend.schoolManagement.email

import mu.KotlinLogging
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import javax.mail.util.ByteArrayDataSource

interface NotificationService {
    fun sendNotification(
        receivers: List<String>,
        subject: String,
        templateType: TemplateType,
        context: Map<String, String>?,
        attachment: EmailAttachment? = null,
        ccReceivers: List<String> = emptyList()
    ): Boolean
}

@Service
class NotificationServiceMailServerImpl(private var mailServerConfig: MailServerConfig) : NotificationService {

    override fun sendNotification(
        receivers: List<String>,
        subject: String,
        templateType: TemplateType,
        context: Map<String, String>?,
        attachment: EmailAttachment?,
        ccReceivers: List<String>
    ): Boolean {
        val logger = KotlinLogging.logger {}

        return kotlin.runCatching {
            val mailSender = getMailSender(mailServerConfig)
            val mimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, true, "utf-8")
            val message = constructHtml(templateType, context).also {
                if (it.isBlank()) {
                    logger.error { "Constructing html failed. Mail not sent." }
                    return false
                }
            }
            helper.setTo(receivers.toTypedArray())
            ccReceivers.forEach { helper.setCc(it) }
            helper.setSubject(subject)
            helper.setText(message, true)
            attachment?.let { helper.addAttachment(it.fileName, ByteArrayDataSource(it.file, it.type)) }

            logger.info(
                "Sending mail with mail server to receivers: {}, with subject: {}, and message: {}.",
                receivers,
                subject,
                message
            )

            mailSender.send(mimeMessage)

            logger.info("Mail sent to receivers")
            true
        }.getOrElse {
            logger.error("Mail send failed for recievers: {}, with subject: {}", receivers, subject, it)
            false
        }
    }

    private fun getMailSender(mailServerConfig: MailServerConfig): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = mailServerConfig.host
        mailSender.port = mailServerConfig.port
        mailSender.username = null
        mailSender.password = null

        mailSender.javaMailProperties.let { props ->
            props["mail.transport.protocol"] = "smtp"
            props["mail.smtp.auth"] = "false"
            props["mail.debug"] = "true"

        }

        return mailSender
    }

    private fun constructHtml(templateType: TemplateType, context: Map<String, String>?): String {
        val htmlContent = when (templateType) {
            TemplateType.RESET_PASSWORD -> javaClass.getResource("/mail/reset-password-template.html")
            TemplateType.EVALUATION_ENTER -> javaClass.getResource("/mail/add-evaluation-template.html")
            TemplateType.EVALUATION_UPDATE -> javaClass.getResource("/mail/update-evaluation-template.html")
        }?.readText() ?: ""

        return context?.toList()?.fold(htmlContent) { html, (key, value) ->
            html.replace("##${key}##", value)
        } ?: ""
    }
}