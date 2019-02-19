package com.project.socialconnect.security

import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Service
class JwtAuthEntryPoint: AuthenticationEntryPoint {

    private val logger = LoggerFactory.getLogger(JwtAuthEntryPoint::class.java)

    override fun commence(request: HttpServletRequest?, response: HttpServletResponse?, authException: AuthenticationException?) {
        logger.error("Unauthorized error. Message - {}", authException)
        response!!.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error -> Unauthorized");
    }

}