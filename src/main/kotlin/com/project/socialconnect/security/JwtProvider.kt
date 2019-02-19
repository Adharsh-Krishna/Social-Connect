package com.project.socialconnect.security

import com.project.socialconnect.constants.SecurityConstants
import io.jsonwebtoken.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import java.util.*


class JwtProvider {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(JwtProvider::class.java.name)
    }

    private val jwtSecret = SecurityConstants.SECRET
    private val jwtExpiration = SecurityConstants.EXPIRATION_TIME

    fun generateJwtToken(authentication: Authentication): String {
        return Jwts.builder()
                .setSubject(authentication.principal as String)
                .setIssuedAt(Date())
                .setExpiration(Date(Date().time + jwtExpiration))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact()
    }

    fun getUserNameFromJwtToken(token: String): String {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .body.subject
    }

    fun validateJwtToken(authToken: String): Boolean {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken)
            return true
        } catch (e: SignatureException) {
            logger.error("Invalid JWT signature -> Message: {} ", e)
        } catch (e: MalformedJwtException) {
            logger.error("Invalid JWT token -> Message: {}", e)
        } catch (e: ExpiredJwtException) {
            logger.error("Expired JWT token -> Message: {}", e)
        } catch (e: UnsupportedJwtException) {
            logger.error("Unsupported JWT token -> Message: {}", e)
        } catch (e: IllegalArgumentException) {
            logger.error("JWT claims string is empty -> Message: {}", e)
        }
        return false
    }
}