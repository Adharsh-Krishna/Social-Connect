package com.project.socialconnect.security

import com.project.socialconnect.repositories.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(private val userRepository: UserRepository): UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val applicationUser = userRepository.findByUserName(username)
                .orElseThrow { UsernameNotFoundException("User Name not found") }
        return UserPrinciple.build(applicationUser)
    }
}