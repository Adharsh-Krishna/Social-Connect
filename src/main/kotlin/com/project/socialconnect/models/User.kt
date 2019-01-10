package com.project.socialconnect.models

import com.project.socialconnect.payloads.user.UserResponsePayload
import org.hibernate.annotations.DynamicUpdate
import javax.persistence.*

@Entity
@Table(name = "users")
@DynamicUpdate
class User(
            @Column(nullable = false)
            private var firstName: String?,

            @Column(nullable = true)
            private var lastName: String?,

            @Column(nullable = false, unique = true)
            private val userName: String?,

            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private  var id: Long?,

            @OneToMany(cascade = [CascadeType.REMOVE], orphanRemoval = true)
            private var accounts: MutableList<Account>?) {


    constructor(firstName: String?, lastName: String?, userName: String?)
            : this(firstName,lastName,userName, null, null)

    fun getId(): Long? {
        return this.id
    }

    fun setId(id: Long) {
        this.id = id
    }

    fun setFirstName(name: String) {
        this.firstName = name
    }

    fun getFirstName(): String? {
        return this.firstName
    }

    fun setLastName(name: String) {
        this.lastName = name
    }

    fun getLastName(): String? {
        return this.lastName
    }

    fun getUserName(): String? {
        return this.userName
    }

    fun addAccount(account: Account) {
        this.accounts?.add(account)
    }

    fun removeAccount(account: Account) {
        this.accounts?.remove(account)
    }

    fun getAccounts(): List<Account>? {
        return this.accounts
    }

    fun toUserResponsePayload(): UserResponsePayload {
        return UserResponsePayload(this.id, this.firstName, this.lastName, this.accounts?.size)
    }
}