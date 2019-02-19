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
    fun existsByUserName(userName: String): Optional<User>

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.firstName = :firstName, u.lastName = :lastName WHERE u.id = :userId")
    fun updateFirstAndLastName(@Param("userId") userId: Long,
                               @Param("firstName") firstName: String,
                               @Param("lastName") lastName: String): Int
}