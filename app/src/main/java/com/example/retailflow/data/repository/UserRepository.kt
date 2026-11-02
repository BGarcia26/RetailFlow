package com.example.retailflow.data.repository

import com.example.retailflow.data.local.UserDao
import com.example.retailflow.data.local.UserEntity
import com.example.retailflow.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UserRepository(private val dao: UserDao) {

    /** CREATE: crea la cuenta. Devuelve el id nuevo o un error con mensaje para mostrar. */
    suspend fun register(fullName: String, email: String, password: String): Result<Int> {
        val cleanEmail = email.trim().lowercase()
        if (dao.findByEmail(cleanEmail) != null) {
            return Result.failure(IllegalStateException("Ya existe una cuenta con ese correo"))
        }
        // El hash es trabajo de CPU: se hace en Dispatchers.Default, no en el hilo principal
        val salt = PasswordHasher.newSalt()
        val hash = withContext(Dispatchers.Default) { PasswordHasher.hash(password, salt) }
        return try {
            val id = dao.insert(
                UserEntity(
                    fullName = fullName.trim(),
                    email = cleanEmail,
                    passwordHash = hash,
                    salt = salt
                )
            )
            Result.success(id.toInt())
        } catch (e: Exception) {
            Result.failure(IllegalStateException("No se pudo crear la cuenta. Intenta nuevamente"))
        }
    }

    /** READ: devuelve el usuario si correo y contraseña coinciden, o null. */
    suspend fun login(email: String, password: String): UserEntity? {
        val user = dao.findByEmail(email.trim().lowercase()) ?: return null
        val ok = withContext(Dispatchers.Default) {
            PasswordHasher.verify(password, user.salt, user.passwordHash)
        }
        return if (ok) user else null
    }

    fun observeUser(id: Int): Flow<UserEntity?> = dao.observeById(id)

    suspend fun getPhotoPath(id: Int): String? = dao.getPhotoPath(id)

    /** UPDATE */
    suspend fun updatePhoto(id: Int, path: String?) = dao.updatePhoto(id, path)
}
