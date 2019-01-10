package com.project.socialconnect.controllers

import com.project.socialconnect.composers.ResponseComposer
import com.project.socialconnect.constants.ErrorConstants
import com.project.socialconnect.models.Account
import com.project.socialconnect.payloads.account.AccountResponsePayload
import com.project.socialconnect.payloads.account.CreateUserAccountRequestPayload
import com.project.socialconnect.repositories.AccountRepository
import com.project.socialconnect.repositories.AccountTypeRepository
import com.project.socialconnect.repositories.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.math.BigInteger

@Controller
class AccountController(private val accountRepository: AccountRepository,
                        private val userRepository: UserRepository,
                        private val accountTypeRepository: AccountTypeRepository) {

    @PostMapping("/users/{userId}/accounts")
    @ResponseBody
    fun createAccount(@RequestBody account: CreateUserAccountRequestPayload, @PathVariable("userId") userId: Long): ResponseEntity<Any> {
        val accountType = accountTypeRepository.findById(account.accountType)
        val user = userRepository.findById(userId)
        val existingAccount = accountRepository.findByUserIdAndAccountName(userId, account.name)
        return when {
            existingAccount.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_ALREADY_EXISTS)
            !accountType.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_TYPE_NOT_FOUND)
            !user.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.USER_NOT_FOUND)
            else -> {
                val newAccount = accountRepository.save(Account(accountType.get(), account.name))
                user.get().addAccount(newAccount)
                userRepository.save(user.get())
                ResponseComposer.composeSuccessResponseWith(newAccount)
            }
        }
    }

    @GetMapping("/users/{userId}/accounts")
    @ResponseBody
    fun listAccounts(@PathVariable("userId") userId: Long): ResponseEntity<Any> {
        val user = userRepository.findById(userId)
        val accounts  = userRepository.findAllAccountsByUserId(userId)
                .map { a -> AccountResponsePayload(a[0] as BigInteger, a[1] as String, a[2] as BigInteger)}
        return when {
            !user.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.USER_NOT_FOUND)
            else -> ResponseComposer.composeSuccessResponseWith(accounts)
        }
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