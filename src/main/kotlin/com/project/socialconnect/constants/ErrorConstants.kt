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
    }

}