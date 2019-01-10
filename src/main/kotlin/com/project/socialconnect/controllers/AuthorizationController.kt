package com.project.socialconnect.controllers

import com.project.socialconnect.authenticators.GoogleAuthenticator
import com.project.socialconnect.models.AccountCredential
import com.project.socialconnect.repositories.AccountCredentialRepository
import com.project.socialconnect.repositories.AccountRepository
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
class AuthorizationController(
        private val accountCredentialRepository: AccountCredentialRepository,
        private val accountRepository: AccountRepository,
        private val googleAuthenticator: GoogleAuthenticator = GoogleAuthenticator()) {

    @GetMapping("/accounts/{accountId}/authorize")
    @ResponseBody
    fun authorizeAccount(@PathVariable("accountId") accountId: Long): AccountCredential? {
        val accountCredential = accountCredentialRepository.findByAccountId(accountId)
        val account = accountRepository.findById(accountId)
        return when {
            !account.isPresent -> null
            !accountCredential.isPresent -> null
            else -> {
                googleAuthenticator.authorize()
                val credential = googleAuthenticator.getCredential()
                val newAccountCredential = AccountCredential(account.get(), credential.accessToken, credential.refreshToken,
                        credential.expiresInSeconds, "Bearer")
                accountCredentialRepository.save(newAccountCredential)
            }
        }
    }

}