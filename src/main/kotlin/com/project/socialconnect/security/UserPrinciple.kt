package com.project.socialconnect.security

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.socialconnect.models.User
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.stream.Collectors;

class UserPrinciple(private val serialVersionID: Long = 1L,
                    private val id: Long,
                    private val firstName: String,
                    private val lastName: String,
                    private val userName: String,
                    @JsonIgnore private val password: String,
                    private val authorities: MutableCollection<GrantedAuthority>): UserDetails {

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun getPassword(): String {
        return this.password
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun getUsername(): String {
        return this.userName
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return this.authorities
    }

    override fun isEnabled(): Boolean {
        return true
    }

    companion object {
        fun build(user: User): UserPrinciple {
            val authorities: MutableList<GrantedAuthority> = user.getRoles()
                    .stream()
                    .map { role ->
                        SimpleGrantedAuthority("ROLE_" + role.getName())
                    }
                    .collect(Collectors.toList())

            return UserPrinciple(
                    id = user.getId()!!,
                    firstName = user.getFirstName()!!,
                    lastName = user.getLastName()!!,
                    userName = user.getUserName()!!,
                    password = user.getPassword()!!,
                    authorities = authorities)
        }
    }
}