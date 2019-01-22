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
import com.project.socialconnect.models.AccountCredential
import org.springframework.mock.web.MockMultipartFile
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import javax.activation.MimetypesFileTypeMap

@Service
class GoogleDriveService: CloudService() {
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
                .setFields("nextPageToken, files(id, name, parents, kind, mimeType)")
                .setQ("mimeType != 'application/vnd.google-apps.folder'")
                .setPageSize(pageSize)
                .execute()
        return result.files!!
    }

    override fun listAllFolders(folderId: String?, folderPath: String?): Any {
       return  service.files().list()
                .setQ("mimeType = 'application/vnd.google-apps.folder' and '$folderId' in parents")
                .execute().files
    }

    override fun uploadFile(file: MultipartFile, fileName: String): File {
        val fileStream = ByteArrayInputStream(file.bytes)
        val contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(fileName)!!
        val newFile = File()
        newFile.name = fileName
        return service.files()
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

    override fun deleteFile(fileId: String?, fileName: String?): Any {
        service.files().delete(fileId).execute()
        return "deleted"
    }

    override fun transferToDropbox(accountCredential: AccountCredential, fileId: String?, folderPath: String?, folderId: String?): Any {
        val outputStream = downloadFile(fileId)!!
        val dropboxService = DropboxService()
        val accessToken = accountCredential.getAccessToken()
        dropboxService.setCredential(accessToken)
        val googleFileDetails = getFileDetails(fileId, null)
        val multipartFile = MockMultipartFile(googleFileDetails.name, outputStream.toByteArray())
        val uploadedFile = dropboxService.uploadFile(multipartFile, folderPath + "/" + googleFileDetails.name)
        deleteFile(fileId, null)
        return uploadedFile
    }

    override fun getFileDetails(fileId: String?, fileName: String?): File{
        return service.files().get(fileId).execute()
    }

    override fun transferToGoogleDrive(accountCredential: AccountCredential, fileName: String?, folderPath: String?, folderId: String?): Any {
        return ""
    }

}

