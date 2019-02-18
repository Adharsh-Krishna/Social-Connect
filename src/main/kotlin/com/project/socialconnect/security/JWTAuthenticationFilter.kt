package com.project.socialconnect.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.project.socialconnect.security.SecurityConstants.EXPIRATION_TIME
import com.project.socialconnect.security.SecurityConstants.HEADER_STRING
import com.project.socialconnect.security.SecurityConstants.SECRET
import com.project.socialconnect.security.SecurityConstants.TOKEN_PREFIX
import com.project.socialconnect.models.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.io.IOException
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JWTAuthenticationFilter : UsernamePasswordAuthenticationFilter {

    constructor(authenticationManager: AuthenticationManager) : super() {
        this.authenticationManager = authenticationManager
    }

    @Throws(AuthenticationException::class)
    override fun attemptAuthentication(req: HttpServletRequest,
                                       res: HttpServletResponse): Authentication {

        try {
            val user = ObjectMapper()
                    .readValue(req.inputStream, User::class.java)
            val userName = user.getUserName()!!
            return this.authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken(
                            userName,
                            user.getPassword(),
                            ArrayList<GrantedAuthority>())
            )
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    @Throws(IOException::class, ServletException::class)
    override fun successfulAuthentication(req: HttpServletRequest,
                                                    res: HttpServletResponse,
                                                    chain: FilterChain,
                                                    auth: Authentication) {

        val userName = auth.name
        val token = Jwts.builder()
                .setSubject(userName)
                .setExpiration(Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact()

        res.addHeader(HEADER_STRING, TOKEN_PREFIX + token)
    }
}