package com.project.socialconnect.constants

abstract class ErrorConstants {
    companion object {
        const val USER_NOT_FOUND = "User could not be found"
        const val USER_ALREADY_EXISTS_WITH_USER_NAME = "User already exists with the user name"
        const val RECORD_COULD_NOT_BE_UPDATED = "Record could not be updated. Unknown error."
    }

}