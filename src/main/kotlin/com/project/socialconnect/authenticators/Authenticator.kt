package com.project.socialconnect.authenticators

interface Authenticator{
    fun authorize(): Any
    fun reAuthorize(refreshToken: String): Any
}