package com.project.socialconnect.controllers

import com.project.socialconnect.composers.ResponseComposer
import com.project.socialconnect.constants.ErrorConstants
import com.project.socialconnect.factories.CloudServiceFactory
import com.project.socialconnect.repositories.AccountCredentialRepository
import com.project.socialconnect.repositories.AccountRepository
import com.project.socialconnect.repositories.UserRepository
import com.project.socialconnect.services.CloudService
import kategory.getOrElse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.activation.MimetypesFileTypeMap
import javax.servlet.http.HttpServletResponse


@Controller
class FileController(private val accountRepository: AccountRepository,
                     private val accountCredentialRepository: AccountCredentialRepository,
                     private val userRepository: UserRepository) {

    @GetMapping("/accounts/{accountId}/files")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @ResponseBody
    fun listFiles(@PathVariable("accountId") accountId: Long,
                  @RequestParam("pageSize") pageSize: Int?): Any {
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
                                .getOrElse { throw Exception(ErrorConstants.COULD_NOT_FETCH_FILES + " from " + accountType) }
                        ResponseComposer.composeSuccessResponseWith(files)
                    }
                }
            }
        }
    }

    @GetMapping("/accounts/{accountId}/folders")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
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
                                .getOrElse { throw Exception(ErrorConstants.COULD_NOT_FETCH_FOLDERS) }
                        ResponseComposer.composeSuccessResponseWith(folders)
                    }
                }
            }
        }
    }

    @PostMapping("/accounts/{accountId}/files")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
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
                                .getOrElse { throw Exception(ErrorConstants.COULD_NOT_UPLOAD_FILE_TO_DROPBOX) }
                        ResponseComposer.composeSuccessResponseWith(newFile)
                    }
                }
            }
        }
    }


    @GetMapping("/accounts/{accountId}/files/download")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @ResponseBody
    fun downloadFile(@PathVariable("accountId") accountId: Long,
                     @RequestParam("fileId", required = false) fileId: String?,
                     @RequestParam("fileName") fileName: String?,
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
                        service.getFileDetails(fileId, fileName)
                                .getOrElse { throw Exception(ErrorConstants.DROPBOX_FILE_NOT_FOUND) }
                        val downloadedFile = service.downloadFile(fileId, fileName)
                                .getOrElse { throw Exception(ErrorConstants.COULD_NOT_DOWNLOAD_FILE_FROM_GOOGLE) }
                        val content = downloadedFile.toByteArray()!!
                        val mediaType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(fileName)!!
                        ResponseComposer.sendFileWithSuccessResponse(fileName!!, mediaType, content)
                    }
                }
            }
        }
    }


    @PostMapping("/accounts/files/transfer")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
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
                        val transferredFile = service.transferTo(receiverAccount.get().getAccountType()!!.getName()!!, receiverAccountCredential.get(), fileId, fileName, folderPath, folderId)
                                .getOrElse { throw Exception("transfer failed") }
                        ResponseComposer.composeSuccessResponseWith(transferredFile!!)
                    }
                }
            }
        }
    }


    @GetMapping("users/{userId}/files")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @ResponseBody
    fun listAllFiles(@PathVariable("userId") userId: Long,
                     @RequestParam("filesPerAccount") filesPerAccount: Int? = null): ResponseEntity<Any> {
        val user = userRepository.findById(userId)
        return when {
            !user.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.USER_NOT_FOUND)
            else -> {
                val files = CloudService.fetchAllFiles(user.get(), accountCredentialRepository, filesPerAccount)
                        .getOrElse { throw Exception(ErrorConstants.UNKNOWN_ERROR) }
                ResponseComposer.composeSuccessResponseWith(files)
            }
        }
    }
}