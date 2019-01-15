package com.project.socialconnect.authenticators

import com.google.api.client.auth.oauth2.*
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.BasicAuthentication
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.DriveScopes
import com.project.socialconnect.constants.GoogleDriveConstants
import com.project.socialconnect.models.Account
import com.project.socialconnect.models.AccountCredential
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

@Service
class GoogleAuthenticator: Authenticator() {

    private val  jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
    private val scopes: List<String> = Collections.singletonList(DriveScopes.DRIVE)
    private val  httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()

    override fun authorize(accountId: Long) {
        val input: InputStream? = File(GoogleDriveConstants.CREDENTIALS_FILE_PATH).inputStream()
        val clientSecrets: GoogleClientSecrets = GoogleClientSecrets.load(jsonFactory, InputStreamReader(input))
        authorizationUrl = AuthorizationRequestUrl(clientSecrets.details.authUri, clientSecrets.details.clientId, Arrays.asList("code"))
                .setState(accountId.toString())
                .setRedirectUri(clientSecrets.details.redirectUris.first())
                .set("access_type", GoogleDriveConstants.ACCESS_TYPE)
                .setScopes(scopes)
                .build()
    }

    override fun returnAccountCredential(account: Account, code: String): AccountCredential {
        val input: InputStream? = File(GoogleDriveConstants.CREDENTIALS_FILE_PATH).inputStream()
        val clientSecrets: GoogleClientSecrets = GoogleClientSecrets.load(jsonFactory, InputStreamReader(input))
        val credentials = AuthorizationCodeTokenRequest(httpTransport, jsonFactory, GenericUrl(clientSecrets.details.tokenUri), code)
                .setRedirectUri(clientSecrets.details.redirectUris.first())
                .setClientAuthentication(BasicAuthentication(clientSecrets.details.clientId, clientSecrets.details.clientSecret))
                .execute()
        return AccountCredential(account, credentials.accessToken, credentials.refreshToken, credentials.expiresInSeconds, credentials.tokenType)

    }

    override fun reAuthorizeAndReturnAccountCredential(accountCredential: AccountCredential): AccountCredential {
        val refreshToken = accountCredential.getRefreshToken()
        val input: InputStream? = File(GoogleDriveConstants.CREDENTIALS_FILE_PATH).inputStream()
        val clientSecrets: GoogleClientSecrets = GoogleClientSecrets.load(jsonFactory, InputStreamReader(input))
        val clientId = clientSecrets.details.clientId
        val clientSecret = clientSecrets.details.clientSecret
        val response = GoogleRefreshTokenRequest(httpTransport, jsonFactory, refreshToken, clientId, clientSecret).execute()
        accountCredential.setAccessToken(response.accessToken)
        return accountCredential
    }

    override fun extractAccountId(state: String): String {
        return state
    }
}