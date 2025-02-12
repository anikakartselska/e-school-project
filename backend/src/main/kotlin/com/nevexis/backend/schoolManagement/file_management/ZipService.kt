package com.nevexis.backend.schoolManagement.file_management

import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Service
class ZipService {
    fun zipFiles(files: List<SmsFile>): ByteArray =
        ByteArrayOutputStream().use { byteArrayOutputStream ->
            ZipOutputStream(byteArrayOutputStream).use { zipOutputStream ->
                files.forEach { file ->
                    val entry = ZipEntry(file.fileName)
                    zipOutputStream.putNextEntry(entry)
                    zipOutputStream.write(file.fileContent!!)
                    zipOutputStream.closeEntry()
                }
            }
            byteArrayOutputStream.toByteArray()
        }
}