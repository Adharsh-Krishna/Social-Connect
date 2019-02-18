package com.project.socialconnect.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.project.socialconnect.auth.SecurityConstants.EXPIRATION_TIME
import com.project.socialconnect.auth.SecurityConstants.HEADER_STRING
import com.project.socialconnect.auth.SecurityConstants.SECRET
import com.project.socialconnect.auth.SecurityConstants.TOKEN_PREFIX
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


//    private lateinit var authenticationManager: AuthenticationManager

    constructor(authenticationManager: AuthenticationManager) : super() {
        this.authenticationManager = authenticationManager
//        this.authenticationManager = authenticationManager
    }

    @Throws(AuthenticationException::class)
    override fun attemptAuthentication(req: HttpServletRequest,
                                       res: HttpServletResponse): Authentication {

        try {
            val creds = ObjectMapper()
                    .readValue(req.inputStream, User::class.java)
            val userName = creds.getUserName()!!
            println("-->$creds")
            println("auth...${this.authenticationManager}")
            return this.authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken(
                            userName,
                            creds.getPassword(),
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

        val user = auth.name
        println("userrrr --- >$user")
//        val userName = user.getUserName()!!
        val token = Jwts.builder()
                .setSubject(user)
                .setExpiration(Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact()

        res.addHeader(HEADER_STRING, TOKEN_PREFIX + token)
    }
}