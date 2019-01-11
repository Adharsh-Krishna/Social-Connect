package com.project.socialconnect.controllers

import com.project.socialconnect.authenticators.GoogleAuthenticator
import com.project.socialconnect.composers.ResponseComposer
import com.project.socialconnect.constants.ErrorConstants
import com.project.socialconnect.models.AccountCredential
import com.project.socialconnect.repositories.AccountCredentialRepository
import com.project.socialconnect.repositories.AccountRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
class AuthorizationController(
        private val accountCredentialRepository: AccountCredentialRepository,
        private val accountRepository: AccountRepository,
        private val googleAuthenticator: GoogleAuthenticator = GoogleAuthenticator()) {

    @GetMapping("/accounts/{accountId}/authorize")
    @ResponseBody
    fun authorizeAccount(@PathVariable("accountId") accountId: Long): ResponseEntity<Any> {
        val account = accountRepository.findById(accountId)
        return when {
            !account.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_FOUND)
            else -> {
                val accountCredential = accountCredentialRepository.findByAccountId(accountId)
                return when {
                    accountCredential.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_ALREADY_AUTHORIZED)
                    else -> {
                        googleAuthenticator.authorize()
                        val credential = googleAuthenticator.getCredential()
                        val newAccountCredential = AccountCredential(account.get(), credential.accessToken, credential.refreshToken,
                                credential.expiresInSeconds, "Bearer")
                        val accountCredentialAfterSave = accountCredentialRepository.save(newAccountCredential)
                        ResponseComposer.composeSuccessResponseWith(accountCredentialAfterSave)

                    }
                }
            }
        }
    }

}