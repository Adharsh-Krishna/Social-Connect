package com.project.socialconnect.repositories


import com.project.socialconnect.models.Account
import com.project.socialconnect.models.User
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*
import javax.transaction.Transactional

interface UserRepository : CrudRepository<User, Long> {

    fun findByUserName(userName: String): Optional<User>

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.firstName = :firstName, u.lastName = :lastName WHERE u.id = :userId")
    fun updateFirstAndLastName(@Param("userId") userId: Long,
                               @Param("firstName") firstName: String,
                               @Param("lastName") lastName: String): Int

    @Transactional
    @Query("SELECT accounts.id, accounts.name, accounts.account_type_id FROM accounts INNER JOIN users_accounts ON users_accounts.user_id = ?1 AND users_accounts.accounts_id = accounts.id", nativeQuery = true)
    fun findAllAccountsByUserId(userId: Long): Array<Array<Any>>
}