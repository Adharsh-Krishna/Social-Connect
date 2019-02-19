package com.project.socialconnect.constants

object SecurityConstants {
    const val SECRET = "SecretKeyToGenJWTs"
    const val EXPIRATION_TIME: Long = 86400
    const val TOKEN_PREFIX = "Bearer "
    const val HEADER_STRING = "Authorization"
    const val SIGN_UP_URL = "/users/sign-up"
    const val SIGN_IN_URL = "/users/sign-in"
}
