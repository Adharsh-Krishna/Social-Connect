package com.project.socialconnect.models

import org.junit.Assert.assertEquals
import org.junit.Test
class UserTest {
    init {

    }

    @Test fun shouldCreateNewUser() {
        val firstName = "testFirstName"
        val lastName = "testLastName"
        val userName = "testUserName"
        val password = "123abc"
        val accounts = null
        val newUser = User(firstName, lastName, userName, password)
        assertEquals(newUser.getAccounts(), accounts)
        assertEquals(newUser.getUserName(), userName)
        assertEquals(newUser.getLastName(), lastName)
        assertEquals(newUser.getFirstName(), firstName)
    }
}