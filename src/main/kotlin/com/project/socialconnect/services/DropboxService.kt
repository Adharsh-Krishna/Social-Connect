package com.project.socialconnect.services

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.GetMetadataErrorException
import com.dropbox.core.v2.files.Metadata
import com.project.socialconnect.constants.DropboxConstants
import com.project.socialconnect.models.AccountCredential
import org.springframework.mock.web.MockMultipartFile
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream


@Service
class DropboxService: CloudService() {
    override fun getFileDetails(fileId: String?, fileName: String?): Metadata {
        return service.files().getMetadata(fileName)
    }

    private val requestConfig = DbxRequestConfig(DropboxConstants.CLIENT_IDENTIFIER)
    private lateinit var service: DbxClientV2

    override fun setCredential(accessToken: String) {
        service = DbxClientV2(requestConfig, accessToken)
    }

    override fun listFiles(pageSize: Int?): Any {
        val files = service.files().listFolder("")
        if(pageSize == null) {
            return files
                    .entries
                    .filter { it !is FolderMetadata}
        }
        return files
                .entries
                .asSequence()
                .filter { it !is FolderMetadata }
                .take(pageSize).toList()
    }

    override fun checkIfFileExists(fileId: String?, fileName: String?): Boolean {
        val metadata: Metadata = try {
            service.files().getMetadata(fileName)
        } catch (e: GetMetadataErrorException) {
            return !(e.errorValue.isPath && e.errorValue.pathValue.isNotFound)
        }
        return true
    }

    override fun uploadFile(file: MultipartFile, fileName: String): Any {
        val inputStream = BufferedInputStream(file.inputStream)
        return service
                .files()
                .uploadBuilder(fileName)
                .uploadAndFinish(inputStream)
    }

    override fun downloadFile(fileId: String?, fileName: String?): ByteArrayOutputStream? {
        val outputStream = ByteArrayOutputStream()
        service.files().downloadBuilder(fileName).download(outputStream)
        return outputStream
    }

    override fun deleteFile(fileId: String?, fileName: String?): Any {
        return service.files().deleteV2(fileName)
    }

    override fun transferToDropbox(accountCredential: AccountCredential, fileId: String?, folderPath: String?, folderId: String?): Any {
        return ""
    }

    override fun listAllFolders(folderId: String?, folderPath: String?): Any {
        val actualFolderPath = if (folderPath == "root") "" else folderPath
        return service.files()
                .listFolder(actualFolderPath)
                .entries
                .takeWhile { it is FolderMetadata }
    }

    override fun transferToGoogleDrive(accountCredential: AccountCredential, fileName: String?, folderPath: String?, folderId: String?): Any {
        val accessToken = accountCredential.getAccessToken()
        val googleDriveService = GoogleDriveService()
        googleDriveService.setCredential(accessToken)
        val outputStream = downloadFile(null, fileName)!!
        val dropboxFileDetails = getFileDetails(null, fileName)
        val multipartFile = MockMultipartFile(dropboxFileDetails.name, outputStream.toByteArray())
        val transferredFile = googleDriveService.uploadFile(multipartFile, dropboxFileDetails.name)
        deleteFile(null, fileName)
        return transferredFile
    }

}