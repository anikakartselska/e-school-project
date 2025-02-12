package com.nevexis.backend.schoolManagement.file_management

import com.nevexis.backend.error_handling.SMSError
import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.`demo-project`.jooq.tables.records.FilesEvaluationRecord
import com.nevexis.`demo-project`.jooq.tables.records.FilesRecord
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.FILES
import com.nevexis.`demo-project`.jooq.tables.references.FILES_EVALUATION
import com.nevexis.`demo-project`.jooq.tables.references.USER
import org.jooq.DSLContext
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

    fun saveFileForEvaluations(
        extractedFiles: Map<Pair<String, List<Int>>, ByteArray>,
        createdById: BigDecimal
    ) {
        db.transaction { transaction ->
            val allEvaluationIds = extractedFiles.keys.map { it.second }.flatten().map { it.toBigDecimal() }

            deleteAllFilesForEvaluations(allEvaluationIds, transaction.dsl())

            val recordsToInsert = extractedFiles.map { (fileNameToEvaluationIds, fileContent) ->
                val fileSeqNextVal = getFilesSeqNextVal()

                val newFileRecord = transaction.dsl().newRecord(FILES).apply {
                    id = fileSeqNextVal
                    this.fileContent = byteArrayToBase64String(fileContent)
                    this.fileName = fileNameToEvaluationIds.first
                    this.note = "Извинителна бележка"
                    createdBy = createdById
                    createdOn = LocalDateTime.now()
                }


                newFileRecord to fileNameToEvaluationIds.second.map {
                    transaction.dsl().newRecord(FILES_EVALUATION)
                        .apply {
                            this.filesId = fileSeqNextVal
                            this.evaluationId = it.toBigDecimal()
                        }
                }
            }

            transaction.dsl().batchInsert(recordsToInsert.map { it.first }).execute()
            transaction.dsl().batchInsert(recordsToInsert.map { it.second }.flatten()).execute()
        }
    }

    fun getAllFiles(
        assignmentId: BigDecimal? = null,
        studentSchoolClassId: BigDecimal? = null
    ): List<SmsFile> {
        return selectFilesConditionStep().where()
            .apply {
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

    fun getFileByEvaluationId(
        evaluationId: BigDecimal
    ): SmsFile? {
        return selectFilesConditionStepForEvaluations()
            .where(FILES_EVALUATION.EVALUATION_ID.eq(evaluationId))
            .fetchAny()?.map { mapToInternalModel(it) }

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
            this.assignmentId = assignmentId
            this.studentSchoolClassId = studentSchoolClassId
        }

    }

    fun selectFilesConditionStep() = db.select(
        FILES.asterisk(),
        USER.asterisk(),
    )
        .from(FILES)
        .leftJoin(USER)
        .on(USER.ID.eq(FILES.CREATED_BY))

    fun selectFilesConditionStepForEvaluations(dslContext: DSLContext = db) =
        dslContext.select(FILES.asterisk(), FILES_EVALUATION.asterisk(), USER.asterisk())
            .from(FILES)
            .leftJoin(FILES_EVALUATION)
            .on(FILES.ID.eq(FILES_EVALUATION.FILES_ID))
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

    fun deleteAllFiles(
        assignmentIds: List<BigDecimal>? = null,
        studentSchoolClassIds: List<BigDecimal>? = null
    ) {
        val files = db.selectFrom(FILES).where()
            .apply {
                if (!assignmentIds.isNullOrEmpty()) {
                    and(FILES.ASSIGNMENT_ID.`in`(assignmentIds))
                }
                if (!studentSchoolClassIds.isNullOrEmpty()) {
                    and(FILES.STUDENT_SCHOOL_CLASS_ID.`in`(studentSchoolClassIds))
                }
            }.fetch()

        db.batchDelete(files).execute()
    }

    fun deleteAllFilesForEvaluations(
        evaluationIds: List<BigDecimal>,
        dslContext: DSLContext
    ) {
        dslContext.transaction { transaction ->
            val records = selectFilesConditionStepForEvaluations(transaction.dsl())
                .where(FILES_EVALUATION.EVALUATION_ID.`in`(evaluationIds))
                .fetch()

            val fileRecords = records.map { it.into(FilesRecord::class.java) }
            val filesEvaluationRecords = records.map { it.into(FilesEvaluationRecord::class.java) }

            transaction.dsl().batchDelete(filesEvaluationRecords).execute()

            val existingFileEvaluations =
                if (fileRecords.isNotEmpty()) {
                    transaction.dsl().selectFrom(FILES_EVALUATION)
                        .where(FILES_EVALUATION.EVALUATION_ID.`in`(fileRecords.map { it.id }))
                        .fetch()
                        .groupBy { it.filesId }
                } else {
                    emptyMap()
                }

            fileRecords.filter { existingFileEvaluations[it.id] == null }.apply {
                if (this.isNotEmpty()) {
                    transaction.dsl().batchDelete(this).execute()
                }
            }
        }

    }
}