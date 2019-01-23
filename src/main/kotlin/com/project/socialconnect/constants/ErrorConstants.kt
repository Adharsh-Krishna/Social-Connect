package com.project.socialconnect.constants

abstract class ErrorConstants {
    companion object {
        const val USER_NOT_FOUND = "User could not be found"
        const val USER_ALREADY_EXISTS_WITH_USER_NAME = "User already exists with the user name"
        const val RECORD_COULD_NOT_BE_UPDATED = "Record could not be updated. Unknown error."
        const val ACCOUNT_TYPE_NOT_FOUND = "Account Type could not be found"
        const val ACCOUNT_ALREADY_EXISTS = "Account already exists for this user with the given name"
        const val UNKNOWN_ERROR = "Unknown error"
        const val ACCOUNT_NOT_FOUND = "Account not found"
        const val ACCOUNT_ALREADY_AUTHORIZED = "Account has already been authorized"
        const val ACCOUNT_NOT_AUTHORIZED = "Account has not been authorized"
        const val RE_AUTH_NOT_APPLICABLE = "Re auth not applicable"
        const val FILE_NAME_OR_FILE_ID_MUST_BE_PASSED = "File id/name must be passed as paramater"
        const val DROPBOX_FILE_NOT_FOUND = "Dropbox File not found"
        const val GOOGLE_FILE_NOT_FOUND = "Google File not found"
        const val COULD_NOT_DOWNLOAD_FILE_FROM_GOOGLE = "Could not download file from Google Drive"
        const val COULD_NOT_DOWNLOAD_FILE_FROM_DROPBOX = "Could not download file from Dropbox"
        const val COULD_NOT_UPLOAD_FILE_TO_GOOGLE = "Could not upload file to Google Drive"
        const val COULD_NOT_UPLOAD_FILE_TO_DROPBOX = "Could not upload file to Dropbox"
        const val COULD_NOT_DELETE_FILE_FROM_DROPBOX = "Could not delete file from Dropbox"
        const val COULD_NOT_DELETE_FILE_FROM_GOOGLE = "Could not delete file from Google"
        const val COULD_NOT_FETCH_FILES = "Could not fetch files"
        const val COULD_NOT_FETCH_FOLDERS = "Could not fetch folders"
    }

}