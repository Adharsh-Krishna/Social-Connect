package com.project.socialconnect.constants

abstract class GoogleDriveConstants {
    companion object {
        const val APPLICATION_NAME: String= "Social Connect Application"
        const val TOKENS_DIRECTORY_PATH: String = "tokens"
        const val CREDENTIALS_FILE_PATH: String = "credentials.json"
        const val LOCAL_SERVER_PORT: Int = 8888
        const val ACCESS_TYPE = "offline"
        const val USER_ID = "user"
    }

}