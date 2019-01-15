package com.project.socialconnect.authenticators

import com.google.api.client.auth.oauth2.BearerToken
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.DriveScopes
import com.project.socialconnect.constants.GoogleDriveConstants
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

@Service
class GoogleAuthenticator: Authenticator {

    private val  jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
    private val scopes: List<String> = Collections.singletonList(DriveScopes.DRIVE)
    private val  httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
    private lateinit var credential: Credential

    fun getCredential(): Credential {
        return this.credential
    }

    override fun authorize() {
        val input: InputStream? = File(GoogleDriveConstants.CREDENTIALS_FILE_PATH).inputStream()
        val clientSecrets: GoogleClientSecrets = GoogleClientSecrets.load(jsonFactory, InputStreamReader(input))

        val flow: GoogleAuthorizationCodeFlow =  GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, scopes)
                .setAccessType(GoogleDriveConstants.ACCESS_TYPE)
                .build()
        val receiver: LocalServerReceiver = LocalServerReceiver
                .Builder()
                .setPort(GoogleDriveConstants.LOCAL_SERVER_PORT)
                .build()
        credential = AuthorizationCodeInstalledApp(flow, receiver).authorize(GoogleDriveConstants.USER_ID)
    }

    override fun reAuthorize(refreshToken: String) {
        val input: InputStream? = File(GoogleDriveConstants.CREDENTIALS_FILE_PATH).inputStream()
        val clientSecrets: GoogleClientSecrets = GoogleClientSecrets.load(jsonFactory, InputStreamReader(input))
        val clientId = clientSecrets.details.clientId
        val clientSecret = clientSecrets.details.clientSecret
        val response = GoogleRefreshTokenRequest(httpTransport, jsonFactory, refreshToken, clientId, clientSecret).execute()
        credential = GoogleCredential().setAccessToken(response.accessToken)
    }
}