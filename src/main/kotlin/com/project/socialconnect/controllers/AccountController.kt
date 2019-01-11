package com.project.socialconnect.controllers

import com.project.socialconnect.composers.ResponseComposer
import com.project.socialconnect.constants.ErrorConstants
import com.project.socialconnect.models.Account
import com.project.socialconnect.payloads.account.CreateUserAccountRequestPayload
import com.project.socialconnect.repositories.AccountRepository
import com.project.socialconnect.repositories.AccountTypeRepository
import com.project.socialconnect.repositories.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
class AccountController(private val accountRepository: AccountRepository,
                        private val userRepository: UserRepository,
                        private val accountTypeRepository: AccountTypeRepository) {

    @PostMapping("/users/{userId}/accounts")
    @ResponseBody
    fun createAccount(@RequestBody account: CreateUserAccountRequestPayload, @PathVariable("userId") userId: Long): ResponseEntity<Any> {
        val accountType = accountTypeRepository.findById(account.accountTypeId)
        val user = userRepository.findById(userId)
        return when {
            !user.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.USER_NOT_FOUND)
            !accountType.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_TYPE_NOT_FOUND)
            else -> {
                val existingAccount = user.get().getAccountByName(account.name)
                return when(existingAccount == null) {
                    false -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_ALREADY_EXISTS)
                    else -> {
                        user.get().addAccount(Account(accountType.get(), account.name))
                        userRepository.save(user.get())
                        ResponseComposer.composeSuccessResponseWith(user.get().getAccountByName(account.name)!!.toAccountResponsePayload())
                    }
                }

            }
        }
    }

    @GetMapping("/users/{userId}/accounts")
    @ResponseBody
    fun listAccounts(@PathVariable("userId") userId: Long): ResponseEntity<Any> {
        val user = userRepository.findById(userId)
        val acc = user.get().getAccounts()?.map { it.toAccountResponsePayload()}
        return when {
            !user.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.USER_NOT_FOUND)
            else -> ResponseComposer.composeSuccessResponseWith(acc!!)
        }
    }

    @PutMapping("/users/{userId}/accounts/{accountId}")
    @ResponseBody
    fun updateAccount(@PathVariable("userId") userId: Long,
                      @PathVariable("accountId") accountId: Long,
                      @RequestBody updatedAccount: CreateUserAccountRequestPayload): ResponseEntity<Any> {
        val user = userRepository.findById(userId)
        return when {
            !user.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.USER_NOT_FOUND)
            else -> {
                val account  = user.get().getAccountById(accountId)
                return when (account) {
                    null -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_FOUND)
                    else -> {
                        account.setName(updatedAccount.name)
                        val accountAfterUpdate = accountRepository.save(account)
                        ResponseComposer.composeSuccessResponseWith(accountAfterUpdate.toAccountResponsePayload())
                    }
                }
            }

        }
    }

    @DeleteMapping("/users/{userId}/accounts/{accountId}")
    @ResponseBody
    fun deleteAccount(@PathVariable("userId") userId: Long,
                      @PathVariable("accountId") accountId: Long): ResponseEntity<Any> {
        val user = userRepository.findById(userId)
        return when {
            !user.isPresent -> ResponseComposer.composeErrorResponseWith(ErrorConstants.USER_NOT_FOUND)
            else -> {
                val account = user.get().getAccountById(accountId)
                return when(account) {
                    null -> ResponseComposer.composeErrorResponseWith(ErrorConstants.ACCOUNT_NOT_FOUND)
                    else -> {
                        user.get().removeAccount(account)
                        userRepository.save(user.get())
                        ResponseComposer.composeSuccessResponseWith("DELETED")
                    }
                }
            }
        }
    }
}