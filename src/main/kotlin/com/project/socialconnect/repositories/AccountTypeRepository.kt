package com.project.socialconnect.repositories

import com.project.socialconnect.models.AccountType
import org.springframework.data.repository.CrudRepository

interface AccountTypeRepository: CrudRepository<AccountType, Long>