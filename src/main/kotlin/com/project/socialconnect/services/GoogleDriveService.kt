package com.project.socialconnect.services

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.FileList
import com.project.socialconnect.constants.GoogleDriveConstants
import org.springframework.stereotype.Service

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

    private fun listFiles() {
        val result: FileList = service.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute()
        val files: MutableList<com.google.api.services.drive.model.File>? = result.getFiles()
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.")
        } else {
            System.out.println("Files:")
            files.map {file -> System.out.printf("%s (%s)\n", file.getName(), file.getId()) }
        }
    }

}