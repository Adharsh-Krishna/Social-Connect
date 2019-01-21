package com.project.socialconnect.services

import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream

interface CloudService {
    fun setCredential(accessToken: String)
    fun listFiles(pageSize: Int): Any
    fun createFile(file: MultipartFile, fileName: String): Any
    fun downloadFile(fileId: String? = null, fileName: String? = null): ByteArrayOutputStream?
    fun checkIfFileExists(fileId: String? = null, fileName: String? = null): Boolean
}