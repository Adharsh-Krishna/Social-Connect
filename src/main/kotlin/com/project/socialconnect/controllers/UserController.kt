package com.project.socialconnect.controllers

import com.project.socialconnect.composers.ResponseComposer
import com.project.socialconnect.constants.ErrorConstants
import com.project.socialconnect.models.User
import com.project.socialconnect.payloads.auth.SignInRequest
import com.project.socialconnect.payloads.auth.SignUpRequest
import com.project.socialconnect.payloads.jwt.JwtResponse
import com.project.socialconnect.repositories.RoleRepository
import com.project.socialconnect.repositories.UserRepository
import com.project.socialconnect.security.JwtProvider
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import javax.validation.Valid


@Controller
class UserController(private val userRepository: UserRepository,
                     private val roleRepository: RoleRepository){

    private val bCryptPasswordEncoder = BCryptPasswordEncoder()
    private val jwtProvider: JwtProvider = JwtProvider()


    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    fun listUsers(): ResponseEntity<Any> {
        val users = userRepository.findAll().map { it.toUserResponsePayload()}
        return ResponseComposer.composeSuccessResponseWith(users)
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @ResponseBody
    fun getUser(@PathVariable("id") id: Long): ResponseEntity<Any> {
        val user = userRepository.findById(id)
        return when {
            user.isPresent -> ResponseComposer.composeSuccessResponseWith(user.get().toUserResponsePayload())
            else -> ResponseComposer.composeErrorResponseWith(ErrorConstants.USER_NOT_FOUND)
        }
    }


    @PostMapping("/users/sign-up")
    @ResponseBody
    fun registerUser(@Valid @RequestBody signUpRequest: SignUpRequest): ResponseEntity<Any> {
        if (!userRepository.existsByUserName(signUpRequest.userName!!).isPresent) {
            return ResponseComposer.composeErrorResponseWith("User Name is already present")
        }

        val roleNames = signUpRequest.roles!!
        val roles = roleNames.map { name ->  roleRepository.findByName(name).get()}
        val user = User(signUpRequest.firstName, signUpRequest.lastName, signUpRequest.userName,
                bCryptPasswordEncoder.encode(signUpRequest.password), roles.toSet())
        userRepository.save(user)

        return ResponseComposer.composeSuccessResponseWith(user)
    }

    @PostMapping("/users/sign-in")
    @ResponseBody
    fun authenticateUser(@Valid @RequestBody loginRequest: SignInRequest): ResponseEntity<Any> {

        val authenticationManager =  AuthenticationManager { it }
        val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                        loginRequest.userName,
                        loginRequest.password))

        SecurityContextHolder.getContext().authentication = authentication

        val jwt = jwtProvider.generateJwtToken(authentication)
        return ResponseEntity.ok(JwtResponse(jwt))
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @ResponseBody
    fun updateUser(@RequestBody user: User, @PathVariable("id") id: Long): ResponseEntity<Any> {
        val existingUser = userRepository.findById(id)
        return when {
            !existingUser.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.USER_NOT_FOUND)
            else -> when(userRepository.updateFirstAndLastName(id, user.getFirstName()!!, user.getLastName()!!)) {
                1 -> ResponseComposer.composeSuccessResponseWith("User updated successfully")
                else -> ResponseComposer.composeErrorResponseWith(ErrorConstants.RECORD_COULD_NOT_BE_UPDATED)
            }
        }
    }
}