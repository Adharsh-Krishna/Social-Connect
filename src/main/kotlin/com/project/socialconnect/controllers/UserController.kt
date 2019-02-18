package com.project.socialconnect.controllers

import com.project.socialconnect.composers.ResponseComposer
import com.project.socialconnect.models.User
import com.project.socialconnect.constants.ErrorConstants
import com.project.socialconnect.repositories.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.mortbay.jetty.security.Password.getPassword
import org.springframework.web.bind.annotation.RequestBody





@Controller
class UserController(private val userRepository: UserRepository){

    private val bCryptPasswordEncoder: BCryptPasswordEncoder = BCryptPasswordEncoder()

    @GetMapping("/users")
    @ResponseBody
    fun listUsers(): ResponseEntity<Any> {
        val users = userRepository.findAll().map { it.toUserResponsePayload()}
        return ResponseComposer.composeSuccessResponseWith(users)
    }

    @GetMapping("/users/{id}")
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
    fun createUser(@RequestBody user: User): ResponseEntity<Any> {
        val existingUser = userRepository.findByUserName(user.getUserName()!!)
        return when {
            existingUser.isPresent -> ResponseComposer.composeSuccessResponseWith(ErrorConstants.USER_ALREADY_EXISTS_WITH_USER_NAME)
            else -> {
                val encryptedPassword = bCryptPasswordEncoder.encode(user.getPassword())
                val newUser = userRepository.save(User(user.getFirstName(), user.getLastName(), user.getUserName(), encryptedPassword))
                ResponseComposer.composeSuccessResponseWith(newUser)
            }
        }
    }

    @PutMapping("/users/{id}")
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