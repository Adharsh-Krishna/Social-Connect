package com.project.socialconnect.models

import javax.persistence.*

@Entity
@Table(name = "account_types")
data class AccountType(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private var id: Long?,

        @Column(nullable = false, unique = true)
        private var name: String?,

        @Column(nullable = false)
        private var description: String?) {


    constructor(name: String?, description: String?): this(name= name, description = description, id = null)

    fun getId(): Long? {
        return this.id
    }

    fun setId(id: Long) {
        this.id = id
    }

    fun setName(name: String?) {
        this.name = name
    }

    fun setDescription(description: String?) {
        this.description = description
    }

    fun getName(): String? {
        return this.name
    }

    fun getDescription(): String? {
        return this.description
    }
}