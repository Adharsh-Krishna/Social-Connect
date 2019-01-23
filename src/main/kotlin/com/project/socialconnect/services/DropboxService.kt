package com.project.socialconnect.services

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.Metadata
import com.project.socialconnect.constants.DropboxConstants
import com.project.socialconnect.constants.ErrorConstants
import com.project.socialconnect.models.AccountCredential
import kategory.Try
import kategory.getOrElse
import org.springframework.mock.web.MockMultipartFile
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream

@Service
class DropboxService: CloudService() {

    private val requestConfig = DbxRequestConfig(DropboxConstants.CLIENT_IDENTIFIER)
    private lateinit var service: DbxClientV2


    override fun setCredential(accessToken: String) {
        service = DbxClientV2(requestConfig, accessToken)
    }

    override fun getFileDetails(fileId: String?, fileName: String?): Try<Metadata> {
        return Try {
            service.files().getMetadata("/$fileName")
        }
    }
    override fun listFiles(pageSize: Int?): Try<Any> {
        return Try {
            val folderResult = service.files().listFolder("")
            if(pageSize == null) {
                folderResult
                        .entries
                        .filter { it !is FolderMetadata}
            } else {
                folderResult
                        .entries
                        .asSequence()
                        .filter { it !is FolderMetadata }
                        .take(pageSize).toList()
            }
        }
    }

    override fun uploadFile(file: MultipartFile, fileName: String): Try<Any> {
        val inputStream = BufferedInputStream(file.inputStream)
        return Try {
            service.files().uploadBuilder(fileName).uploadAndFinish(inputStream)
        }
    }

    override fun downloadFile(fileId: String?, fileName: String?): Try<ByteArrayOutputStream> {
        val outputStream = ByteArrayOutputStream()
        return Try {
            service.files().downloadBuilder("/$fileName").download(outputStream)
            outputStream
        }
    }

    override fun deleteFile(fileId: String?, fileName: String?): Try<Any> {
        return Try {
            service.files().deleteV2(fileName)
        }
    }

    override fun transferToDropbox(accountCredential: AccountCredential, fileId: String?, folderPath: String?, folderId: String?): Try<Any> {
        return Try{}
    }

    override fun listAllFolders(folderId: String?, folderPath: String?): Try<Any> {
        val actualFolderPath = folderPath?: ""
        return Try {
            service.files()
                    .listFolder(actualFolderPath)
                    .entries
                    .takeWhile { it is FolderMetadata }
        }
    }

    override fun transferToGoogleDrive(accountCredential: AccountCredential, fileName: String?, folderPath: String?, folderId: String?): Try<Any> {
        val accessToken = accountCredential.getAccessToken()
        val googleDriveService = GoogleDriveService()
        googleDriveService.setCredential(accessToken)
        return Try {
            val dropboxFileDetails = getFileDetails(null, fileName)
                    .getOrElse { throw Exception(ErrorConstants.DROPBOX_FILE_NOT_FOUND) }
            val outputStream = downloadFile(null, fileName)
                    .getOrElse { throw Exception(ErrorConstants.COULD_NOT_DOWNLOAD_FILE_FROM_DROPBOX) }
            val multipartFile = MockMultipartFile(dropboxFileDetails.name, outputStream.toByteArray())
            val transferredFile = googleDriveService.uploadFile(multipartFile, dropboxFileDetails.name)
                    .getOrElse { throw Exception(ErrorConstants.COULD_NOT_UPLOAD_FILE_TO_GOOGLE) }
            deleteFile(null, fileName)
                    .getOrElse { throw Exception(ErrorConstants.COULD_NOT_DELETE_FILE_FROM_DROPBOX) }
            transferredFile
        }
    }

}