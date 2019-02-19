package com.project.socialconnect.security

import com.project.socialconnect.constants.SecurityConstants
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

class JwtAuthTokenFilter(private val userDetailsService: UserDetailsServiceImpl): OncePerRequestFilter() {

    private val jwtTokenProvider: JwtProvider? = JwtProvider()


    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        try {

            val jwt = getJwt(request)
            if (jwt != null && jwtTokenProvider!!.validateJwtToken(jwt)) {
                val username = jwtTokenProvider.getUserNameFromJwtToken(jwt)
                val userDetails = userDetailsService.loadUserByUsername(username)

                val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: Exception) {
            logger.error("Can not set user authentication -> Message: {}", e)
        }


        filterChain.doFilter(request, response)
    }

    private fun getJwt(request: HttpServletRequest): String? {
        val authHeader = request.getHeader(SecurityConstants.HEADER_STRING)

        return if (authHeader != null && authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            authHeader.replace(SecurityConstants.TOKEN_PREFIX, "")
        } else null

    }

}