package com.project.socialconnect.repositories;

import com.project.socialconnect.models.Role
import org.springframework.data.repository.CrudRepository
import java.util.*

interface RoleRepository: CrudRepository<Role, Long> {
    fun findByName(name: String): Optional<Role>
}


