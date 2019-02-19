package com.project.socialconnect.controllers

import com.project.socialconnect.composers.ResponseComposer
import com.project.socialconnect.constants.ErrorConstants
import com.project.socialconnect.models.AccountType
import com.project.socialconnect.repositories.AccountTypeRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
class AccountTypeController(private val accountTypeRepository: AccountTypeRepository) {

    @PostMapping("/account-type")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    fun createAccountType(@RequestBody accountType: AccountType): ResponseEntity<Any> {
        val newAccountType = accountTypeRepository.save(AccountType(accountType.getName(), accountType.getDescription()))
        return ResponseComposer.composeSuccessResponseWith(newAccountType)
    }

    @GetMapping("/account-types")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    fun listAccountTypes(): ResponseEntity<Any> {
        val accountTypes = accountTypeRepository.findAll().map { it }
        return ResponseComposer.composeSuccessResponseWith(accountTypes)
    }

    @PutMapping("account-types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    fun updateAccountType(@RequestBody accountType: AccountType, @PathVariable("id") id: Long): ResponseEntity<Any> {
        val updatedAccountType = accountTypeRepository.findById(id)
        return when {
            !updatedAccountType.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_TYPE_NOT_FOUND)
            else -> {
                updatedAccountType.get().setName(accountType.getName())
                updatedAccountType.get().setDescription(accountType.getDescription())
                val accountTypeAfterUpdate = accountTypeRepository.save(updatedAccountType.get())
                ResponseComposer.composeSuccessResponseWith(accountTypeAfterUpdate)
            }
        }
    }
}