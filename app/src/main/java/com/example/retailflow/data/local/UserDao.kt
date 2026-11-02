package com.example.retailflow.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // CREATE: ABORT lanza excepción si el correo ya existe (índice único)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    // READ
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun observeById(id: Int): Flow<UserEntity?>

    @Query("SELECT photoPath FROM users WHERE id = :id")
    suspend fun getPhotoPath(id: Int): String?

    // UPDATE
    @Query("UPDATE users SET photoPath = :path WHERE id = :id")
    suspend fun updatePhoto(id: Int, path: String?)

    @Query("UPDATE users SET fullName = :name WHERE id = :id")
    suspend fun updateName(id: Int, name: String)
}
