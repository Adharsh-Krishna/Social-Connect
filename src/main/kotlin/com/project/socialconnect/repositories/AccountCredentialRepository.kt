package com.project.socialconnect.repositories

import com.project.socialconnect.models.AccountCredential
import org.springframework.data.repository.CrudRepository
import java.util.*

interface AccountCredentialRepository: CrudRepository<AccountCredential, Long> {

    fun findByAccountId(accountId: Long): Optional<AccountCredential>
}