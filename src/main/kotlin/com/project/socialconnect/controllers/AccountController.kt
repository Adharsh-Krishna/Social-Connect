package com.project.socialconnect.controllers

import com.project.socialconnect.models.Account
import com.project.socialconnect.payloads.account.CreateUserAccountRequestPayload
import com.project.socialconnect.repositories.AccountRepository
import com.project.socialconnect.repositories.AccountTypeRepository
import com.project.socialconnect.repositories.UserRepository
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
class AccountController(private val accountRepository: AccountRepository,
                        private val userRepository: UserRepository,
                        private val accountTypeRepository: AccountTypeRepository) {

    @PostMapping("/users/{userId}/accounts")
    @ResponseBody
    fun createAccount(@RequestBody account: CreateUserAccountRequestPayload, @PathVariable("userId") userId: Long): Account {
            accountTypeRepository.findById(account.accountType)
                .map { accountType -> accountRepository.save(Account(accountType, account.name)) }
                    .map { newAccount -> val user = userRepository.findById(userId).get()
                            user.addAccount(newAccount)
                            userRepository.save(user)}

        return accountRepository.findByName(account.name)
    }

    @GetMapping("/users/{userId}/accounts")
    @ResponseBody
    fun listAccounts(@PathVariable("userId") userId: Long): List<Account>? {
        return userRepository.findById(userId).get().getAccounts()
    }

    @PutMapping("/users/{userId}/accounts/{accountId}")
    @ResponseBody
    fun updateAccount(@PathVariable("userId") userId: Long,
                      @PathVariable("accountId") accountId: Long,
                      @RequestBody updatedAccount: CreateUserAccountRequestPayload): Account {
        val account = accountRepository.findById(accountId).get()
        account.setName(updatedAccount.name)
        return accountRepository.save(account)
    }

    @DeleteMapping("/users/{userId}/accounts/{accountId}")
    @ResponseBody
    fun deleteAccount(@PathVariable("userId") userId: Long,
                      @PathVariable("accountId") accountId: Long): String {
        val user = userRepository.findById(userId).get()
        val account = accountRepository.findById(accountId).get()
        user.removeAccount(account)
        userRepository.save(user)
        accountRepository.deleteById(accountId)
        return "Deleted"
    }
}