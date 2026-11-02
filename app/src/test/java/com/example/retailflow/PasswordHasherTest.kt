package com.example.retailflow

import com.example.retailflow.util.PasswordHasher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {
    @Test fun verify_acceptsCorrectAndRejectsWrong() {
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash("Clave1234", salt)
        assertTrue(PasswordHasher.verify("Clave1234", salt, hash))
        assertFalse(PasswordHasher.verify("clave1234", salt, hash))
    }

    @Test fun hash_isNotPlainText_andDependsOnSalt() {
        val h1 = PasswordHasher.hash("Clave1234", "aa")
        val h2 = PasswordHasher.hash("Clave1234", "bb")
        assertNotEquals("Clave1234", h1)
        assertNotEquals(h1, h2)
        assertEquals(64, h1.length)           // SHA-256 = 32 bytes = 64 caracteres hex
    }

    @Test fun salt_isRandom() {
        assertNotEquals(PasswordHasher.newSalt(), PasswordHasher.newSalt())
    }
}
