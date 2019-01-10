package com.project.socialconnect.controllers

import com.project.socialconnect.models.AccountType
import com.project.socialconnect.repositories.AccountTypeRepository
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
class AccountTypeController(private val accountTypeRepository: AccountTypeRepository) {

    @PostMapping("/account-type")
    @ResponseBody
    fun createAccountType(@RequestBody accountType: AccountType): AccountType {
        val newAccountType = AccountType(accountType.getName(), accountType.getDescription())
        accountTypeRepository.save(newAccountType)
        return newAccountType
    }

    @GetMapping("/account-types")
    @ResponseBody
    fun listAccountTypes(): List<AccountType> {
        return accountTypeRepository.findAll().map { it }
    }

    @DeleteMapping("account-types/{id}")
    @ResponseBody
    fun deleteAccountType(@PathVariable("id") id: Long): String {
        accountTypeRepository.deleteById(id)
        return "Deleted"
    }
}