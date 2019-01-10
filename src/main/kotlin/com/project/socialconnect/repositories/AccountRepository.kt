package com.project.socialconnect.repositories

import com.project.socialconnect.models.Account
import org.springframework.data.repository.CrudRepository

interface AccountRepository: CrudRepository<Account, Long> {

    fun findByName(name: String): Account
}