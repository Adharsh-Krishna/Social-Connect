package com.project.socialconnect.controllers

import com.project.socialconnect.composers.ResponseComposer
import com.project.socialconnect.constants.ErrorConstants
import com.project.socialconnect.factories.CloudServiceFactory
import com.project.socialconnect.repositories.AccountCredentialRepository
import com.project.socialconnect.repositories.AccountRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import javax.servlet.http.HttpServletResponse


@Controller
class FileController(private val accountRepository: AccountRepository,
                     private val accountCredentialRepository: AccountCredentialRepository) {

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
                        val newFile = service.createFile(file, fileName)
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
}