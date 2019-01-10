package com.project.socialconnect.models

import javax.persistence.*

@Entity
@Table(name = "accounts")
data class Account(
        @OneToOne
        @JoinColumn(name = "account_type_id", nullable = false)
        private var accountType: AccountType?,

        @Column(nullable = false, unique = true)
        private var name: String?,

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private  var id: Long?) {

    constructor(accountType: AccountType?, name: String?)
            : this(accountType = accountType, name = name, id = null)

    fun getName(): String?  = this.name

    fun setName(name: String) {
        this.name = name
    }

    fun getAccountType(): AccountType? = this.accountType
}