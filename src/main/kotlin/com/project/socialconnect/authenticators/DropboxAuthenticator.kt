package com.project.socialconnect.authenticators

import com.dropbox.core.*
import com.project.socialconnect.constants.DropboxConstants
import com.project.socialconnect.models.Account
import com.project.socialconnect.models.AccountCredential
import org.mortbay.jetty.Request.getRequest
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession



@Service
class DropboxAuthenticator: Authenticator() {
    private val appInfo: DbxAppInfo = DbxAppInfo.Reader.readFromFile(DropboxConstants.CREDENTIALS_FILE_PATH)
    private val requestConfig = DbxRequestConfig(DropboxConstants.CLIENT_IDENTIFIER)
    private val webAuth = DbxWebAuth(requestConfig, appInfo)

    override fun authorize(accountId: Long) {
        val session = httpRequest.getSession(true)
        val key = DropboxConstants.DBX_SESSION_STORE_KEY

        val dbxStandardSessionStore = DbxStandardSessionStore(session, key)
        val webAuthRequest = DbxWebAuth.newRequestBuilder()
                .withRedirectUri(DropboxConstants.REDIRECT_URI, dbxStandardSessionStore)
                .withState(accountId.toString())
                .build()
        authorizationUrl = webAuth.authorize(webAuthRequest)
    }

    override fun returnAccountCredential(account: Account, code: String): AccountCredential {
        val credentials = webAuth.finishFromCode(code, DropboxConstants.REDIRECT_URI)
        return AccountCredential(account, credentials.accessToken)
    }

    override fun reAuthorizeAndReturnAccountCredential(accountCredential: AccountCredential) {
    }

    override fun getCredential() {
    }

    override fun extractAccountId(state: String): String {
        val index = state.lastIndexOf("==")
        return state.substring(index + 2)
    }
}