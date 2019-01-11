package com.project.socialconnect.services

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.AbstractInputStreamContent
import com.google.api.client.http.FileContent
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.IOUtils
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.project.socialconnect.constants.GoogleDriveConstants
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Service
class GoogleDriveService {
    private val  jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
    private val  httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
    private lateinit var service: Drive

    fun setCredential(credential: GoogleCredential) {
        this.service = Drive.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName(GoogleDriveConstants.APPLICATION_NAME)
            .build()
    }

    companion object {
        private var instance: GoogleDriveService? = null

        @Synchronized
        fun getSingletonInstance(): GoogleDriveService {
            if (instance == null) {
                instance = GoogleDriveService()
            }
            return instance!!
        }
    }

    fun listFiles(pageSize: Int = GoogleDriveConstants.DEFAULT_FILES_PER_PAGE): MutableList<File> {
        val result: FileList = service.files().list()
                .setPageSize(pageSize)
                .execute()
        return result.files!!
    }

    fun createFile(file: MultipartFile, fileName: String, contentType: String): File  {
        val fileStream: InputStream = ByteArrayInputStream(file.bytes)
        val newFile = File()
        newFile.name = fileName
        return  service
                .files()
                .create(newFile, InputStreamContent(contentType, fileStream))
                .execute()
    }

    fun downloadFile(fileId: String) {
        val outputStream = ByteArrayOutputStream()
        return service.files().get(fileId).executeMediaAndDownloadTo(outputStream)
    }

}