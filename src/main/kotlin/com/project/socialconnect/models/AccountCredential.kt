package com.project.socialconnect.models

import javax.persistence.*

@Entity
@Table(name = "account_credentials")
data class AccountCredential(
        @OneToOne
        @JoinColumn(name = "account_id", nullable = false)
        private var account: Account,

        @Column(nullable = false)
        private var accessToken: String,

        @Column
        private var refreshToken: String?,

        @Column
        private var expiresIn: Long?,

        @Column
        private var tokenType: String?,

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private  var id: Long?) {

    constructor(account: Account, accessToken: String, refreshToken: String, expiresIn: Long, tokenType: String)
            : this(account = account,
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = expiresIn,
            tokenType = tokenType,
            id = null)

    constructor(account: Account, accessToken: String)
            : this(account = account,
            accessToken = accessToken,
            refreshToken = null,
            expiresIn = null,
            tokenType = null,
            id = null
            )

        fun getAccessToken(): String {
            return this.accessToken
        }

        fun setAccessToken(accessToken: String) {
            this.accessToken = accessToken
        }

        fun getRefreshToken(): String {
            return this.refreshToken!!
        }
}