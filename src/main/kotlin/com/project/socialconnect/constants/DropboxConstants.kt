package com.project.socialconnect.constants

abstract class DropboxConstants {
    companion object {
        const val CREDENTIALS_FILE_PATH = "dropbox-credentials.app"
        const val CLIENT_IDENTIFIER = "Social-Connect"
        const val DBX_SESSION_STORE_KEY = "dropbox-auth-csrf-token"
        const val REDIRECT_URI = "http://localhost:8080/accounts/dropbox/code"
    }
}