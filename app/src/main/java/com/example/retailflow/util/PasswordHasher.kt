package com.example.retailflow.util

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Hash de contraseñas: SHA-256 con salt aleatorio por usuario.
 * (En una app real se usaría un algoritmo lento como PBKDF2/bcrypt/Argon2;
 * para este proyecto SHA-256 + salt evita guardar la clave en texto plano.)
 */
object PasswordHasher {
    fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.toHex()
    }

    fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest((salt + password).toByteArray(Charsets.UTF_8)).toHex()
    }

    fun verify(password: String, salt: String, expectedHash: String): Boolean =
        hash(password, salt) == expectedHash

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
