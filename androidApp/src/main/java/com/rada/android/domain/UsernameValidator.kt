package com.rada.android.domain

object UsernameValidator {
    const val MIN_LENGTH = 3
    const val MAX_LENGTH = 24

    private val allowed = Regex("^[A-Za-z0-9._]+$")

    fun error(raw: String): String? {
        val name = raw.trim()
        return when {
            name.isEmpty() -> "Choose a username"
            name.length < MIN_LENGTH -> "Username must be at least $MIN_LENGTH characters"
            name.length > MAX_LENGTH -> "Username can be at most $MAX_LENGTH characters"
            else -> null
        }
    }

    fun accountError(raw: String): String? = error(raw)
        ?: if (!allowed.matches(raw.trim())) "Use only letters, numbers, dots and underscores" else null

    const val MIN_PASSWORD_LENGTH = 8

    fun passwordError(password: String): String? = when {
        password.isEmpty() -> "Enter a password"
        password.length < MIN_PASSWORD_LENGTH -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
        else -> null
    }
}
