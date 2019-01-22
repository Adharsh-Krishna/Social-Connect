package com.project.socialconnect.services

import com.project.socialconnect.models.AccountCredential
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream

abstract class CloudService {
    abstract fun setCredential(accessToken: String)
    abstract fun listFiles(pageSize: Int): Any
    abstract fun uploadFile(file: MultipartFile, fileName: String): Any
    abstract fun downloadFile(fileId: String? = null, fileName: String? = null): ByteArrayOutputStream?
    abstract fun checkIfFileExists(fileId: String? = null, fileName: String? = null): Boolean
    abstract fun transferToDropbox(accountCredential: AccountCredential, fileId: String?, folderPath: String?, folderId: String?): Any
    abstract fun transferToGoogleDrive(accountCredential: AccountCredential, fileName: String?, folderPath: String?, folderId: String?): Any
    abstract fun deleteFile(fileId: String?, fileName: String?): Any
    abstract fun getFileDetails(fileId: String?, fileName: String?): Any
    abstract fun listAllFolders(folderId: String?, folderPath: String?): Any

    fun transferTo(receiverAccountType: String, accountCredential: AccountCredential, fileId: String?, fileName: String?, folderPath: String?, folderId: String?): Any? {
        return when(receiverAccountType) {
            "google" -> return transferToGoogleDrive(accountCredential, fileName, folderPath?: "", folderId)
            "dropbox" -> return transferToDropbox(accountCredential, fileId, folderPath?: "", folderId)
            else -> null
        }
    }

}