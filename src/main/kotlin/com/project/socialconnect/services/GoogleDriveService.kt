package com.project.socialconnect.services

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.project.socialconnect.constants.GoogleDriveConstants
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@Service
class GoogleDriveService: CloudService {
    private val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
    private val httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
    private lateinit var service: Drive

    override fun setCredential(accessToken: String) {
        val credential: GoogleCredential = GoogleCredential().setAccessToken(accessToken)
        this.service = Drive.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(GoogleDriveConstants.APPLICATION_NAME)
                .build()
    }

    override fun listFiles(pageSize: Int): MutableList<File> {
        val result: FileList = service.files().list()
                .setPageSize(pageSize)
                .execute()
        return result.files!!
    }

    override fun createFile(file: MultipartFile, fileName: String): File {
        val fileStream = ByteArrayInputStream(file.bytes)
        val contentType = file.contentType!!
        val newFile = File()
        newFile.name = fileName
        return service
                .files()
                .create(newFile, InputStreamContent(contentType, fileStream))
                .execute()
    }

    override fun downloadFile(fileId: String?, fileName: String?): ByteArrayOutputStream? {
        val outputStream = ByteArrayOutputStream()
        service.files().get(fileId).executeMediaAndDownloadTo(outputStream)
        return outputStream
    }

    override fun checkIfFileExists(fileId: String?, fileName: String?): Boolean {
        return true
    }
}

