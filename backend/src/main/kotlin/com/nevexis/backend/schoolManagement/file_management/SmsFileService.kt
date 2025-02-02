package com.nevexis.backend.schoolManagement.file_management

import com.nevexis.backend.error_handling.SMSError
import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.`demo-project`.jooq.tables.records.FilesRecord
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.FILES
import com.nevexis.`demo-project`.jooq.tables.references.USER
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
class SmsFileService : BaseService() {

    @Autowired
    private lateinit var userService: UserService

    fun saveFile(
        fileContent: ByteArray,
        fileName: String,
        createdById: BigDecimal,
        note: String? = null,
        evaluationId: BigDecimal? = null,
        assignmentId: BigDecimal? = null,
        studentSchoolClassId: BigDecimal? = null,
        fileId: BigDecimal? = null
    ): SmsFile {
        if (fileId == null) {
            val newFileId = getFilesSeqNextVal()
            val newRecord = db.newRecord(FILES).apply {
                id = newFileId
                this.fileContent = byteArrayToBase64String(fileContent)
                this.fileName = fileName
                this.note = note
                createdBy = createdById
                createdOn = LocalDateTime.now()
                this.evaluationId = evaluationId
                this.assignmentId = assignmentId
                this.studentSchoolClassId = studentSchoolClassId
            }
            newRecord.insert()
            val record = selectFilesConditionStep()
                .where(FILES.ID.eq(newFileId))
                .fetchAny() ?: throw SMSError("Несъществуващ файл", "Файлът не беше създаден успешно")

            return mapToInternalModel(record, false)
        } else {
            val fileRecordWithUserRecord = selectFilesConditionStep()
                .where(FILES.ID.eq(fileId))
                .fetchAny()
            val updatedFileRecord = fileRecordWithUserRecord?.into(FilesRecord::class.java)?.apply {
                this.fileContent = byteArrayToBase64String(fileContent)
                this.fileName = fileName
                this.note = note
            } ?: throw SMSError("Несъществуващ файл", "Файлът, който се опитвате да редактирате не съществува")
            updatedFileRecord.update()
            return mapToInternalModel(fileRecordWithUserRecord.apply { from(updatedFileRecord) })
        }
    }

    fun getAllFiles(
        evaluationId: BigDecimal? = null,
        assignmentId: BigDecimal? = null,
        studentSchoolClassId: BigDecimal? = null
    ): List<SmsFile> {
        return selectFilesConditionStep().where()
            .apply {
                if (evaluationId != null) {
                    and(FILES.EVALUATION_ID.eq(evaluationId))
                }
                if (assignmentId != null) {
                    and(FILES.ASSIGNMENT_ID.eq(assignmentId))
                }
                if (studentSchoolClassId != null) {
                    and(FILES.STUDENT_SCHOOL_CLASS_ID.eq(studentSchoolClassId))
                }
            }.fetch().map { mapToInternalModel(it, withFileContent = false) }
    }

    fun getFileById(
        fileId: BigDecimal
    ): SmsFile {
        return selectFilesConditionStep()
            .where(FILES.ID.eq(fileId))
            .fetchAny()?.map { mapToInternalModel(it) }
            ?: throw SMSError("Несъществуващ файл", "Файлът, който се опитвате да достъпите не съществува.")

    }

    fun mapToInternalModel(record: Record, withFileContent: Boolean = true): SmsFile {
        val fileRecord = record.into(FilesRecord::class.java)
        return SmsFile(
            id = fileRecord.id?.toInt(),
            fileContent = if (withFileContent) {
                base64StringToByteArray(fileRecord.fileContent!!)!!
            } else {
                null
            },
            note = fileRecord.note,
            fileName = fileRecord.fileName!!,
            createdBy = userService.mapToUserView(record.into(UserRecord::class.java), emptyList()),
            createdOn = fileRecord.createdOn!!
        )
    }

    fun mapToRecord(
        smsFile: SmsFile,
        evaluationId: BigDecimal? = null,
        assignmentId: BigDecimal? = null,
        studentSchoolClassId: BigDecimal? = null
    ): FilesRecord {
        return db.newRecord(FILES).apply {
            id = getFilesSeqNextVal()
            fileContent = byteArrayToBase64String(smsFile.fileContent)
            fileName = smsFile.fileName
            note = smsFile.note
            createdBy = smsFile.createdBy.id.toBigDecimal()
            createdOn = smsFile.createdOn
            this.evaluationId = evaluationId
            this.assignmentId = assignmentId
            this.studentSchoolClassId = studentSchoolClassId
        }

    }

    fun selectFilesConditionStep() = db.select(
        FILES.asterisk(),
        USER.asterisk()
    )
        .from(FILES)
        .leftJoin(USER)
        .on(USER.ID.eq(FILES.CREATED_BY))

    fun byteArrayToBase64String(byteArray: ByteArray?): String {
        return Base64.getEncoder().encodeToString(byteArray)
    }

    fun base64StringToByteArray(base64String: String?): ByteArray? {
        if (base64String == null) return null
        return Base64.getDecoder().decode(base64String)
    }

    fun getFilesSeqNextVal(): BigDecimal =
        db.select(DSL.field("FILES_SEQ.nextval")).from("DUAL")
            .fetchOne()!!.map { it.into(BigDecimal::class.java) }

    fun deleteFileById(fileId: BigDecimal) {
        db.deleteFrom(FILES)
            .where(FILES.ID.eq(fileId))
            .execute()
    }
}