package com.project.socialconnect.authenticators

import com.project.socialconnect.models.Account
import com.project.socialconnect.models.AccountCredential
import javax.servlet.http.HttpServletRequest

abstract class Authenticator{
    lateinit var authorizationUrl: String
    lateinit var httpRequest: HttpServletRequest
    abstract fun authorize(accountId: Long): Any
    abstract fun reAuthorizeAndReturnAccountCredential(accountCredential: AccountCredential): Any
    abstract fun returnAccountCredential(account: Account, code: String): Any
    abstract fun extractAccountId(state: String): Any
}