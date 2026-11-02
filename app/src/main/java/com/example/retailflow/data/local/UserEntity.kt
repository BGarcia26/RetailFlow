package com.example.retailflow.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla `users`: cuentas creadas desde la pantalla "Crear cuenta".
 * - El correo es único (índice UNIQUE) y se guarda en minúsculas.
 * - La contraseña NUNCA se guarda en texto plano: se guarda hash SHA-256 + salt aleatorio.
 * - photoPath es la ruta del archivo de la foto de perfil dentro del almacenamiento interno de la app.
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val photoPath: String? = null
)
