package com.project.socialconnect.models

import javax.persistence.*

@Entity
@Table(name = "roles")
class Role(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private  var id: Long?,

        @Column()
        private var name: String) {

    constructor(name: String): this(null, name)

    fun getName(): String {
        return this.name
    }

    fun getId(): Long {
        return this.id!!
    }
}