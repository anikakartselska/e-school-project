package com.nevexis.backend.schoolManagement.file_management

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.`demo-project`.jooq.tables.records.FilesRecord
import com.nevexis.`demo-project`.jooq.tables.records.UserRecord
import com.nevexis.`demo-project`.jooq.tables.references.FILES
import com.nevexis.`demo-project`.jooq.tables.references.USER
import org.jooq.Record
import org.springframework.stereotype.Service
import java.util.*

@Service
class SmsFileService : BaseService() {

    fun selectFilesConditionStep() = db.select(
        FILES.asterisk(),
        USER.asterisk()
    )
        .from(FILES)
        .leftJoin(USER)
        .on(USER.ID.eq(FILES.CREATED_BY))

    fun mapToInternalModel(record: Record) {
       val fileRecord = record.into(FilesRecord::class.java)
        return SmsFile(
             id = fileRecord.id?.toInt(),
         fileContent = base64StringToByteArray(fileRecord.fileContent!!)!!,
         fileName = fileRecord.fileName!!,
         createdBy = record.into(UserRecord::class.java),
         createdOn: LocalDateTime
        )
    }

    fun byteArrayToBase64String(byteArray: ByteArray): String {
        return Base64.getEncoder().encodeToString(byteArray)
    }

    fun base64StringToByteArray(base64String: String?): ByteArray? {
        if (base64String == null) return null
        return Base64.getDecoder().decode(base64String)
    }

}