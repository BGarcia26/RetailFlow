package com.example.retailflow.util

/**
 * Reglas de validación de formularios en un solo lugar (lógica pura, sin Android,
 * por eso se puede probar con tests unitarios).
 *
 * Convención: cada `xxxError` devuelve el mensaje de error, o null si el valor es válido
 * O está vacío (un campo vacío no se marca en rojo; el botón se bloquea aparte con isNotBlank).
 */
object Validators {
    const val CORPORATE_DOMAIN = "@rinconsur.com"
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_COUNT = 99

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // Letras (con tildes/ñ) separadas por UN solo espacio, guion o apóstrofe
    private val NAME_REGEX = Regex("^\\p{L}+([ '\\-]\\p{L}+)*$")

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    fun isCorporateEmail(email: String): Boolean =
        isValidEmail(email) && email.trim().lowercase().endsWith(CORPORATE_DOMAIN)

    fun emailError(email: String, requireCorporate: Boolean = false): String? = when {
        email.isEmpty() -> null
        !isValidEmail(email) -> "Formato de correo inválido (ej: nombre$CORPORATE_DOMAIN)"
        requireCorporate && !isCorporateEmail(email) -> "Debe pertenecer al dominio $CORPORATE_DOMAIN"
        else -> null
    }

    fun nameError(name: String): String? = when {
        name.isEmpty() -> null
        name.trim().length < 3 -> "Ingresa al menos 3 caracteres"
        !NAME_REGEX.matches(name.trim()) -> "Solo letras, con un espacio entre palabras"
        else -> null
    }

    fun passwordError(pass: String): String? = when {
        pass.isEmpty() -> null
        pass.length < MIN_PASSWORD_LENGTH -> "Mínimo $MIN_PASSWORD_LENGTH caracteres"
        pass.none { it.isUpperCase() } -> "Debe incluir al menos una mayúscula"
        pass.none { it.isDigit() } -> "Debe incluir al menos un número"
        else -> null
    }

    fun confirmError(pass: String, confirm: String): String? = when {
        confirm.isEmpty() -> null
        pass != confirm -> "Las contraseñas no coinciden"
        else -> null
    }

    /** Fortaleza de 0 a 4: largo, mayúscula, número y símbolo. */
    fun passwordStrength(pass: String): Int {
        var score = 0
        if (pass.length >= MIN_PASSWORD_LENGTH) score++
        if (pass.any { it.isUpperCase() }) score++
        if (pass.any { it.isDigit() }) score++
        if (pass.any { !it.isLetterOrDigit() }) score++
        return score
    }

    /** Convierte el texto de un contador a Int (0..99) o null si no es válido. */
    fun parseCount(text: String): Int? = text.trim().toIntOrNull()?.takeIf { it in 0..MAX_COUNT }

    fun countError(text: String): String? =
        if (parseCount(text) == null) "Ingresa un número entre 0 y $MAX_COUNT" else null
}
