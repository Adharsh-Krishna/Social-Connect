package com.project.socialconnect.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.project.socialconnect.payloads.user.UserResponsePayload
import org.hibernate.annotations.DynamicUpdate
import javax.persistence.*
import java.util.HashSet



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

            @Column(nullable = false)
            @JsonIgnore
            private var password: String?,

            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private  var id: Long?,

            @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
            private var accounts: MutableList<Account>?,

            @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
            @JoinTable(name = "user_roles",
                       joinColumns = [JoinColumn(name = "user_id")],
                       inverseJoinColumns = [JoinColumn(name = "role_id")])
            private var roles: Set<Role> = HashSet()) {



    constructor(firstName: String?, lastName: String?, userName: String?, password: String?, roles: Set<Role>)
            : this(firstName,lastName,userName, password, null, null, roles)

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

    fun getAccountById(id: Long): Account? {
        return this.accounts?.find { account  -> account.getId() == id}
    }

    fun getAccountByName(name: String): Account? {
        return this.accounts?.find { account  -> account.getName() == name}
    }

    fun getPassword(): String? {
        return this.password
    }

    fun setPassword(password: String?) {
        this.password = password
    }

    fun getRoles(): Set<Role> {
        return this.roles
    }

    fun setRoles(roles: Set<Role>) {
        this.roles = roles
    }
}