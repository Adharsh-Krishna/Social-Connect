package com.project.socialconnect.controllers

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.project.socialconnect.composers.ResponseComposer
import com.project.socialconnect.constants.ErrorConstants
import com.project.socialconnect.repositories.AccountCredentialRepository
import com.project.socialconnect.repositories.AccountRepository
import com.project.socialconnect.services.GoogleDriveService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Controller
class FileController(private val accountRepository: AccountRepository,
                     private val accountCredentialRepository: AccountCredentialRepository) {

    @GetMapping("/accounts/{accountId}/files")
    @ResponseBody
    fun listFiles(@PathVariable("accountId") accountId: Long): Any {
        val account = accountRepository.findById(accountId)
        return when {
            !account.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_FOUND)
            else -> {
                val accountCredential = accountCredentialRepository.findByAccountId(accountId)
                return when {
                    !accountCredential.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_AUTHORIZED)
                    else -> {
                        val service: GoogleDriveService = GoogleDriveService.getSingletonInstance()
                        val credential: GoogleCredential = GoogleCredential().setAccessToken(accountCredential.get().getAccessToken())
                        service.setCredential(credential)
                        val files = service.listFiles()
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
                   @RequestParam("contentType") contentType: String,
                   @PathVariable("accountId") accountId: Long): Any {
        val account = accountRepository.findById(accountId)
        return when {
            !account.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_FOUND)
            else -> {
                val accountCredential = accountCredentialRepository.findByAccountId(accountId)
                return when {
                    !accountCredential.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_AUTHORIZED)
                    else -> {
                        val service: GoogleDriveService = GoogleDriveService.getSingletonInstance()
                        val credential: GoogleCredential = GoogleCredential().setAccessToken(accountCredential.get().getAccessToken())
                        service.setCredential(credential)
                        val newFile = service.createFile(file, fileName, contentType)
                        ResponseComposer.composeSuccessResponseWith(newFile)
                    }
                }
            }
        }
    }

//    @GetMapping("/accounts/{accountId}/files/{fileId}/download")
//    @ResponseBody
//    fun downloadFile(@PathVariable("accountId") accountId: Long, @PathVariable("fileId") fileId: String): ResponseEntity<Any> {
//        val account = accountRepository.findById(accountId)
//        return when {
//            !account.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_FOUND)
//            else -> {
//                val accountCredential = accountCredentialRepository.findByAccountId(accountId)
//                return when {
//                    !accountCredential.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_AUTHORIZED)
//                    else -> {
//                        val service: GoogleDriveService = GoogleDriveService.getSingletonInstance()
//                        val credential: GoogleCredential = GoogleCredential().setAccessToken(accountCredential.get().getAccessToken())
//                        service.setCredential(credential)
//                        val downloadedFile = service.downloadFile(fileId)
//                        ResponseComposer.composeSuccessResponseWith(downloadedFile)
//                    }
//                }
//            }
//        }
//    }
}