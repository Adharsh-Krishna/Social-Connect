package com.project.socialconnect.repositories

import com.project.socialconnect.models.Account
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*
import javax.transaction.Transactional

interface AccountRepository: CrudRepository<Account, Long> {

    fun findByName(name: String): Account

    @Transactional
    @Query("SELECT a.id, a.name, a.accountType FROM Account a INNER JOIN User u ON u.id = :userId AND a.name = :accountName")
    fun findByUserIdAndAccountName(@Param("userId") userId: Long, @Param("accountName") accountName: String): Optional<Account>
}