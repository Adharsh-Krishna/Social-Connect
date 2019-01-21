package com.project.socialconnect.factories

import com.project.socialconnect.authenticators.Authenticator
import com.project.socialconnect.authenticators.DropboxAuthenticator
import com.project.socialconnect.authenticators.GoogleAuthenticator

interface AuthenticatorFactory {

    companion object {
        private val authenticators: Map<String, Authenticator> = mutableMapOf(
                Pair("google", GoogleAuthenticator()),
                Pair("dropbox", DropboxAuthenticator())
        )

        private val applicableReAuthenticators: Map<String, Boolean> = mutableMapOf(
            Pair("google", true),
            Pair("dropbox", false)
        )

        fun getAuthenticator(authenticatorName: String): Authenticator {
            return authenticators.get(authenticatorName)!!
        }

        fun isReAuthenticationApplicable(accountType: String): Boolean {
            return applicableReAuthenticators.get(accountType)!!
        }

    }

}