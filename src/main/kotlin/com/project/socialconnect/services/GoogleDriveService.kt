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
import com.project.socialconnect.constants.AppConstants
import com.project.socialconnect.constants.ErrorConstants
import com.project.socialconnect.constants.GoogleDriveConstants
import com.project.socialconnect.models.AccountCredential
import kategory.Try
import kategory.getOrElse
import org.springframework.mock.web.MockMultipartFile
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.activation.MimetypesFileTypeMap

const val GOOGLE_FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"

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

    override fun listFiles(pageSize: Int?): Try<Any> {
        return Try {
            val result: FileList = service.files().list()
                    .setFields("nextPageToken, files(id, name, parents, kind, mimeType)")
                    .setQ("mimeType != '$GOOGLE_FOLDER_MIME_TYPE' and '${AppConstants.ROOT_FOLDER}' in parents")
                    .execute()
            if(pageSize == null) {
                result.files
            } else {
            result.files.take(pageSize) as MutableList<File>
            }
        }
    }

    override fun listAllFolders(folderId: String?, folderPath: String?): Try<Any> {
    val actualFolderId = folderId?: AppConstants.ROOT_FOLDER
       return  Try {
           service.files().list()
                   .setQ("mimeType = '$GOOGLE_FOLDER_MIME_TYPE' and '$actualFolderId' in parents")
                   .execute().files
       }
    }

    override fun uploadFile(file: MultipartFile, fileName: String): Try<File> {
        val fileStream = ByteArrayInputStream(file.bytes)
        val contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(fileName)!!
        val newFile = File()
        newFile.name = fileName
        return Try {
            service.files()
                    .create(newFile, InputStreamContent(contentType, fileStream))
                    .execute()
        }
    }

    override fun downloadFile(fileId: String?, fileName: String?): Try<ByteArrayOutputStream> {
        val outputStream = ByteArrayOutputStream()
        return Try {
            service.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            outputStream
        }
    }

    override fun deleteFile(fileId: String?, fileName: String?): Try<Any> {
        return Try {
            service.files().delete(fileId).execute()
        }
    }

    override fun transferToDropbox(accountCredential: AccountCredential, fileId: String?, folderPath: String?, folderId: String?): Try<Any> {
        val dropboxService = DropboxService()
        val accessToken = accountCredential.getAccessToken()
        dropboxService.setCredential(accessToken)
        return Try {
            val googleFileDetails = getFileDetails(fileId, null)
                    .getOrElse { throw Exception(ErrorConstants.GOOGLE_FILE_NOT_FOUND) }
            val outputStream = downloadFile(fileId)
                    .getOrElse { throw Exception(ErrorConstants.COULD_NOT_DOWNLOAD_FILE_FROM_GOOGLE) }
            val multipartFile = MockMultipartFile(googleFileDetails.name, outputStream.toByteArray())
            val uploadedFile = dropboxService.uploadFile(multipartFile, folderPath + "/" + googleFileDetails.name)
                    .getOrElse { throw Exception(ErrorConstants.COULD_NOT_UPLOAD_FILE_TO_DROPBOX) }
            deleteFile(fileId, null)
                    .getOrElse { throw Exception(ErrorConstants.COULD_NOT_DELETE_FILE_FROM_GOOGLE) }
            uploadedFile
        }
    }

    override fun getFileDetails(fileId: String?, fileName: String?): Try<File> {
        return Try {
            service.files().get(fileId).execute()
        }
    }

    override fun transferToGoogleDrive(accountCredential: AccountCredential, fileName: String?, folderPath: String?, folderId: String?): Try<Any> {
        return Try{}
    }

}

