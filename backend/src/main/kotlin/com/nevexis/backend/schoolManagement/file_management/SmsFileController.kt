package com.nevexis.backend.schoolManagement.file_management

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api")
class SmsFileController {

    @Autowired
    private lateinit var smsFileService: SmsFileService

    @GetMapping("/get-all-files-with-filter-without-file-content")
    fun fetchAllFilesWithFilterWithoutFileContent(
        evaluationId: BigDecimal? = null,
        assignmentId: BigDecimal? = null,
        studentSchoolClassId: BigDecimal? = null
    ): List<SmsFile> = smsFileService.getAllFiles(evaluationId, assignmentId, studentSchoolClassId)

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

    @PostMapping("/upload-file")
    fun uploadFile(
        @RequestPart fileContent: ByteArray,
        fileName: String,
        createdById: BigDecimal,
        note: String? = null,
        evaluationId: BigDecimal? = null,
        assignmentId: BigDecimal? = null,
        studentSchoolClassId: BigDecimal? = null,
        fileId: BigDecimal? = null
    ) = smsFileService.saveFile(
        fileContent, fileName,
        createdById,
        note, evaluationId, assignmentId, studentSchoolClassId, fileId
    )

    @PostMapping("/delete-file")
    fun deleteFile(
        fileId: BigDecimal
    ) = smsFileService.deleteFileById(fileId)
}