package com.nevexis.backend.schoolManagement.file_management

import com.nevexis.backend.error_handling.SMSError
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.ByteArrayInputStream
import java.math.BigDecimal
import java.security.Principal
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class SmsFileController {

    @Autowired
    private lateinit var smsFileService: SmsFileService

    @GetMapping("/get-all-files-with-filter-without-file-content")
    fun fetchAllFilesWithFilterWithoutFileContent(
        assignmentId: BigDecimal? = null,
        studentSchoolClassId: BigDecimal? = null
    ): List<SmsFile> = smsFileService.getAllFiles(assignmentId, studentSchoolClassId)

    @PostMapping("/get-file-by-id")
    fun downloadFile(
        @RequestParam fileId: BigDecimal
    ): ResponseEntity<Resource> {
        val file = smsFileService.getFileById(fileId)

        val resource = ByteArrayResource(
            file.fileContent!!
        )

        return ResponseEntity.status(HttpStatus.OK).headers(
            HttpHeaders().apply {
                set(
                    HttpHeaders.CONTENT_DISPOSITION,
                    """attachment; filename=${file.fileName}""""
                )
            }
        ).contentLength(resource.contentLength())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource)
    }

    @PostMapping("/get-file-by-evaluation-id")
    fun downloadFileByEvaluationId(
        @RequestParam evaluationId: BigDecimal
    ): ResponseEntity<ByteArrayResource>? {
        val file = smsFileService.getFileByEvaluationId(evaluationId)

        val resource = file?.fileContent?.let {
            ByteArrayResource(
                it
            )
        }

        return resource?.contentLength()?.let {
            ResponseEntity.status(HttpStatus.OK).headers(
                HttpHeaders().apply {
                    set(
                        HttpHeaders.CONTENT_DISPOSITION,
                        """attachment; filename=${file.fileName}""""
                    )
                }
            ).contentLength(it)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource)
        }
    }

    @PostMapping("/upload-file")
    fun uploadFile(
        @RequestPart fileContent: ByteArray,
        fileName: String,
        createdById: BigDecimal,
        note: String? = null,
        assignmentId: BigDecimal? = null,
        studentSchoolClassId: BigDecimal? = null,
        fileId: BigDecimal? = null
    ) = smsFileService.saveFile(
        fileContent, fileName,
        createdById,
        note, assignmentId, studentSchoolClassId, fileId
    )

    @PostMapping("/upload-evaluation-files")
    fun uploadFile(
        @RequestPart zipBytes: ByteArray,
        fileIndexToEvaluationIds: String,
        principal: Principal
    ) {
        val fileIndexToEvaluationIdsMap =
            Json.decodeFromString<List<Pair<Int, List<Int>>>>(fileIndexToEvaluationIds).toMap()
        val extractedFiles = mutableMapOf<Pair<String, List<Int>>, ByteArray>()
        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zipIn ->
            var entry: ZipEntry? = zipIn.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val (fileName, index) = extractFilenameAndIndex(entry.name)
                    val fileBytes = zipIn.readBytes()
                    extractedFiles[Pair(fileName, fileIndexToEvaluationIdsMap[index]!!)] = fileBytes
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }
        smsFileService.saveFileForEvaluations(extractedFiles, principal.name.toBigDecimal())
    }

    fun extractFilenameAndIndex(input: String): Pair<String, Int> {
        val regex = """(.+)_(\d+)""".toRegex() // Matches `filename_index`
        val matchResult = regex.matchEntire(input)

        return matchResult?.let {
            val (filename, index) = it.destructured
            filename to index.toInt() // Convert index to Int
        } ?: throw SMSError("Грешна операция", "Името на файла не може да бъде прочетено")
    }

    @PostMapping("/delete-file")
    fun deleteFile(
        fileId: BigDecimal
    ) = smsFileService.deleteFileById(fileId)
}