package com.project.socialconnect.constants
abstract class AppConstants {
    companion object {
        const val GOOGLE = "google"
        const val DROPBOX = "dropbox"
        val accountTypes = listOf<String>(GOOGLE, DROPBOX)
    }
}