package com.project.socialconnect.security

import com.project.socialconnect.constants.SecurityConstants.SIGN_IN_URL
import com.project.socialconnect.constants.SecurityConstants.SIGN_UP_URL
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebSecurity(private val userDetailsService: UserDetailsServiceImpl,
                  private val entryPoint: JwtAuthEntryPoint) : WebSecurityConfigurerAdapter() {

    private val bCryptPasswordEncoder: BCryptPasswordEncoder = BCryptPasswordEncoder()
    private val authenticationJwtTokenFilter: JwtAuthTokenFilter = JwtAuthTokenFilter(userDetailsService)

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.cors().and().csrf().disable().authorizeRequests()
                .antMatchers(HttpMethod.POST, SIGN_UP_URL).permitAll()
                .antMatchers(HttpMethod.POST, SIGN_IN_URL).permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(entryPoint)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        http.addFilterBefore(authenticationJwtTokenFilter, UsernamePasswordAuthenticationFilter::class.java)

    }

    @Throws(Exception::class)
    public override fun configure(authenticationManagerBuilder: AuthenticationManagerBuilder?) {
        authenticationManagerBuilder!!
                .userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder)
    }
}