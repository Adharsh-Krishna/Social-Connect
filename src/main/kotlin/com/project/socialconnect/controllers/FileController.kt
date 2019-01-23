package com.project.socialconnect.controllers

import com.project.socialconnect.composers.ResponseComposer
import com.project.socialconnect.constants.ErrorConstants
import com.project.socialconnect.factories.CloudServiceFactory
import com.project.socialconnect.repositories.AccountCredentialRepository
import com.project.socialconnect.repositories.AccountRepository
import com.project.socialconnect.repositories.UserRepository
import com.project.socialconnect.services.CloudService
import com.project.socialconnect.services.DropboxService
import com.project.socialconnect.services.GoogleDriveService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletResponse


@Controller
class FileController(private val accountRepository: AccountRepository,
                     private val accountCredentialRepository: AccountCredentialRepository,
                     private val userRepository: UserRepository) {

    @GetMapping("/accounts/{accountId}/files")
    @ResponseBody
    fun listFiles(@PathVariable("accountId") accountId: Long,
                  @RequestParam("pageSize") pageSize: Int = 10): Any {
        val account = accountRepository.findById(accountId)
        return when {
            !account.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_FOUND)
            else -> {
                val accountCredential = accountCredentialRepository.findByAccountId(accountId)
                return when {
                    !accountCredential.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_AUTHORIZED)
                    else -> {
                        val accountType = account.get().getAccountType()!!.getName()
                        val accessToken = accountCredential.get().getAccessToken()
                        val service = CloudServiceFactory.getCloudService(accountType!!)
                        service.setCredential(accessToken)
                        val files = service.listFiles(pageSize)
                        ResponseComposer.composeSuccessResponseWith(files)
                    }
                }
            }
        }
    }

    @GetMapping("/accounts/{accountId}/folders")
    @ResponseBody
    fun listFolders(@PathVariable("accountId") accountId: Long,
                    @RequestParam("folderId") folderId: String? = null,
                    @RequestParam("folderPath") folderPath: String? = null): ResponseEntity<Any> {
        val account = accountRepository.findById(accountId)
        return when {
            !account.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_FOUND)
            else -> {
                val accountCredential = accountCredentialRepository.findByAccountId(accountId)
                return when {
                    !accountCredential.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_AUTHORIZED)
                    else -> {
                        val accountType = account.get().getAccountType()!!.getName()
                        val service = CloudServiceFactory.getCloudService(accountType!!)
                        val accessToken = accountCredential.get().getAccessToken()
                        service.setCredential(accessToken)
                        val folders = service.listAllFolders(folderId, folderPath)
                        ResponseComposer.composeSuccessResponseWith(folders)
                    }
                }
            }
        }
    }

    @PostMapping("/accounts/{accountId}/files")
    @ResponseBody
    fun uploadFile(@RequestParam("file") file: MultipartFile,
                   @RequestParam("fileName") fileName: String,
                   @PathVariable("accountId") accountId: Long): Any {
        val account = accountRepository.findById(accountId)
        return when {
            !account.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_FOUND)
            else -> {
                val accountCredential = accountCredentialRepository.findByAccountId(accountId)
                return when {
                    !accountCredential.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_AUTHORIZED)
                    else -> {
                        val accountType = account.get().getAccountType()!!.getName()
                        val accessToken = accountCredential.get().getAccessToken()
                        val service = CloudServiceFactory.getCloudService(accountType!!)
                        service.setCredential(accessToken)
                        val newFile = service.uploadFile(file, fileName)
                        ResponseComposer.composeSuccessResponseWith(newFile)
                    }
                }
            }
        }
    }


    @GetMapping("/accounts/{accountId}/files/download")
    @ResponseBody
    fun downloadFile(@PathVariable("accountId") accountId: Long,
                     @RequestParam("fileId", required = false) fileId: String?,
                     @RequestParam("fileName", required = false) fileName: String?,
                     response: HttpServletResponse): ResponseEntity<Any> {
        val account = accountRepository.findById(accountId)
        return when {
            !account.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_FOUND)
            fileId.isNullOrBlank() && fileName.isNullOrBlank() -> ResponseComposer.composeErrorResponseWith(ErrorConstants.FILE_NAME_OR_FILE_ID_MUST_BE_PASSED)
            else -> {
                val accountCredential = accountCredentialRepository.findByAccountId(accountId)
                return when {
                    !accountCredential.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_AUTHORIZED)
                    else -> {
                        val accountType = account.get().getAccountType()!!.getName()
                        val service = CloudServiceFactory.getCloudService(accountType!!)
                        val accessToken = accountCredential.get().getAccessToken()
                        service.setCredential(accessToken)
                        if (!service.checkIfFileExists(fileId = fileId, fileName = fileName)) {
                            return ResponseComposer.composeErrorResponseWith(ErrorConstants.UNKNOWN_ERROR)
                        }
                        val downloadFile = service.downloadFile(fileId = fileId, fileName = fileName)!!
                        val content = downloadFile.toByteArray()!!
                        ResponseComposer.sendFileWithSuccessResponse("hah.txt", "text/plain", content)
                    }
                }
            }
        }
    }


    @PostMapping("/accounts/files/transfer")
    @ResponseBody
    fun transferFile(@RequestParam("senderId") senderAccountId: Long,
                     @RequestParam("receiverId") receiverAccountId: Long,
                     @RequestParam("fileId") fileId: String? = null,
                     @RequestParam("fileName") fileName: String? = null,
                     @RequestParam("folderPath") folderPath: String? = null,
                     @RequestParam("folderId") folderId: String? = null): ResponseEntity<Any> {
        val senderAccount = accountRepository.findById(senderAccountId)
        val receiverAccount = accountRepository.findById(receiverAccountId)
        return when {
            !senderAccount.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_FOUND)
            !receiverAccount.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_FOUND)
            fileId.isNullOrBlank() && fileName.isNullOrBlank() -> ResponseComposer.composeErrorResponseWith(ErrorConstants.FILE_NAME_OR_FILE_ID_MUST_BE_PASSED)
            else -> {
                val senderAccountCredential = accountCredentialRepository.findByAccountId(senderAccountId)
                val receiverAccountCredential = accountCredentialRepository.findByAccountId(receiverAccountId)
                return when {
                    !senderAccountCredential.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_AUTHORIZED)
                    !receiverAccountCredential.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_AUTHORIZED)
                    else -> {
                        val accountType = senderAccount.get().getAccountType()!!
                        val service = CloudServiceFactory.getCloudService(accountType.getName()!!)
                        val accessToken = senderAccountCredential.get().getAccessToken()
                        service.setCredential(accessToken)
                        val transferredFile = service.transferTo(receiverAccount.get().getAccountType()!!.getName()!!, receiverAccountCredential.get(), fileId, fileName, folderPath, folderId)!!
                        ResponseComposer.composeSuccessResponseWith(transferredFile)
                    }
                }
            }
        }
    }


    @GetMapping("users/{userId}/files")
    @ResponseBody
    fun listAllFiles(@PathVariable("userId") userId: Long,
                     @RequestParam("filesPerAccount") filesPerAccount: Int? = null): ResponseEntity<Any> {
        val user = userRepository.findById(userId)
        return when {
            !user.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.USER_NOT_FOUND)
            else -> {
                val files = CloudService.fetchAllFiles(user.get(), accountCredentialRepository, filesPerAccount)
                ResponseComposer.composeSuccessResponseWith(files)
            }
        }
    }
}