package com.project.socialconnect.services

import com.project.socialconnect.constants.AppConstants
import com.project.socialconnect.factories.CloudServiceFactory
import com.project.socialconnect.models.Account
import com.project.socialconnect.models.AccountCredential
import com.project.socialconnect.models.User
import com.project.socialconnect.repositories.AccountCredentialRepository
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream

const val ID = "id"
const val FILES = "files"

abstract class CloudService {
    abstract fun setCredential(accessToken: String)
    abstract fun listFiles(pageSize: Int?): Any
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
            AppConstants.GOOGLE -> return transferToGoogleDrive(accountCredential, fileName, folderPath?: "", folderId)
            AppConstants.DROPBOX -> return transferToDropbox(accountCredential, fileId, folderPath?: "", folderId)
            else -> null
        }
    }

    companion object {
        fun fetchAllFiles(user: User, accountCredentialRepository: AccountCredentialRepository, filesPerAccount: Int?): MutableMap<String, MutableList<MutableMap<String, Any>>> {
            val accounts = user.getAccounts()!!
            val accountTypes = AppConstants.accountTypes
            val filesByAccountType: MutableMap<String, MutableList<MutableMap<String, Any>>> = hashMapOf()
            accountTypes.forEach { accountType ->
                filesByAccountType[accountType] = mutableListOf()
            }
            accounts.forEach { account ->
                filesByAccountType[account.getAccountType()!!.getName()!!]!!
                        .add(
                                mutableMapOf(
                                        Pair(ID, account.getId()!!),
                                        Pair(FILES, extractFilesBasedOnAccountType(account, accountCredentialRepository, filesPerAccount))
                                )
                        )
            }
            return filesByAccountType
        }

        private fun extractFilesBasedOnAccountType(account: Account, accountCredentialRepository: AccountCredentialRepository, filesPerAccount: Int?): Any {
            val accountType = account.getAccountType()!!.getName()!!
            val service = CloudServiceFactory.getCloudService(accountType)
            val accessToken = accountCredentialRepository.findByAccountId(account.getId()!!).get().getAccessToken()
            service.setCredential(accessToken)
            return service.listFiles(filesPerAccount)
        }
    }

}