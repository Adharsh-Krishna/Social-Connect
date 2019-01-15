package com.project.socialconnect.controllers

import com.project.socialconnect.composers.ResponseComposer
import com.project.socialconnect.constants.ErrorConstants
import com.project.socialconnect.factories.AuthenticatorFactory
import com.project.socialconnect.models.AccountCredential
import com.project.socialconnect.repositories.AccountCredentialRepository
import com.project.socialconnect.repositories.AccountRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@Controller
class AuthorizationController(
        private val accountCredentialRepository: AccountCredentialRepository,
        private val accountRepository: AccountRepository) {

    @GetMapping("/accounts/{accountId}/authorize")
    @ResponseBody
    fun authorizeAccount(@PathVariable("accountId") accountId: Long, request: HttpServletRequest): ResponseEntity<Any> {
        val account = accountRepository.findById(accountId)
        return when {
            !account.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_FOUND)
            else -> {
                val accountCredential = accountCredentialRepository.findByAccountId(accountId)
                return when {
                    accountCredential.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_ALREADY_AUTHORIZED)
                    else -> {
                        val accountType = account.get().getAccountType()!!.getName()!!
                        println(accountType)
                        val authenticator = AuthenticatorFactory.getAuthenticator(accountType)
                        authenticator.httpRequest = request
                        authenticator.authorize(accountId)
                        ResponseComposer.composeSuccessResponseWith(mutableMapOf(Pair("url", authenticator.authorizationUrl)))
                    }
                }
            }
        }
    }

    @GetMapping("/accounts/{accountType}/code")
    @ResponseBody
    fun receiveCode(@PathVariable("accountType") accountType: String,
                    @RequestParam("code") code: String,
                    @RequestParam("state") state: String): ResponseEntity<Any> {
        val authenticator = AuthenticatorFactory.getAuthenticator(accountType)
        val extractedAccountId = authenticator.extractAccountId(state) as String
        val account = accountRepository.findById(extractedAccountId.toLong())
        println(extractedAccountId)
        return when {
            !account.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_FOUND)
            else -> {
                val accountCredential = accountCredentialRepository.findByAccountId(extractedAccountId.toLong())
                return when {
                    accountCredential.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_ALREADY_AUTHORIZED)
                    else -> {
                        val newAccountCredential = authenticator.returnAccountCredential(account.get(), code) as AccountCredential
                        accountCredentialRepository.save(newAccountCredential)
                        ResponseComposer.composeSuccessResponseWith("Account authorized successfully")
                    }
                }
            }
        }
    }

    @GetMapping("/accounts/{accountId}/reauthorize")
    @ResponseBody
    fun reAuthorizeAccount(@PathVariable("accountId") accountId: Long): ResponseEntity<Any> {
        val account = accountRepository.findById(accountId)
        return when {
            !account.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_FOUND)
            !AuthenticatorFactory
                    .isReAuthenticationApplicable(account.get().getAccountType()!!.getName()!!) -> ResponseComposer.composeErrorResponseWith(ErrorConstants.RE_AUTH_NOT_APPLICABLE)
            else -> {
                val accountCredential = accountCredentialRepository.findByAccountId(accountId)
                return when {
                    !accountCredential.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_AUTHORIZED)
                    else -> {
                        val accountType = account.get().getAccountType()!!.getName()
                        val authenticator = AuthenticatorFactory.getAuthenticator(accountType!!)
                        val newAccountCredential = authenticator.reAuthorizeAndReturnAccountCredential(accountCredential.get())
                        accountCredentialRepository.save(newAccountCredential as AccountCredential)
                        ResponseComposer.composeSuccessResponseWith("Re-authorized successfully")
                    }
                }
            }
        }
    }

}