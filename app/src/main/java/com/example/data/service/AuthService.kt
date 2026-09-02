package com.example.data.service

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.security.MessageDigest

data class AuthUser(
    val id: String,
    val email: String,
    val displayName: String,
    val username: String,
    val role: String = "user", // "user", "business", "admin"
    val isEmailVerified: Boolean = true,
    val isAnonymous: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

sealed class AuthResult {
    data class Success(val user: AuthUser) : AuthResult()
    data class Error(val messageEn: String, val messageAr: String, val messageFr: String) : AuthResult()
}

class AuthService(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("waygo_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser

    init {
        // Restore session if exists
        val savedId = prefs.getString("user_id", null)
        val savedEmail = prefs.getString("user_email", null)
        val savedName = prefs.getString("user_name", null)
        val savedUsername = prefs.getString("user_username", null)
        val savedRole = prefs.getString("user_role", "user") ?: "user"

        if (savedId != null && savedEmail != null && savedName != null) {
            _currentUser.value = AuthUser(
                id = savedId,
                email = savedEmail,
                displayName = savedName,
                username = savedUsername ?: "explorer",
                role = savedRole
            )
        } else {
            // Default logged in demo profile for immediate out-of-the-box exploration
            _currentUser.value = AuthUser(
                id = "usr_demo_1",
                email = "ahmed.explorer@waygo.app",
                displayName = "Ahmed",
                username = "ahmed_explorer",
                role = "user"
            )
        }
    }

    fun login(email: String, pass: String): AuthResult {
        if (email.isBlank() || !email.contains("@")) {
            return AuthResult.Error(
                "Please enter a valid email address.",
                "يرجى إدخال بريد إلكتروني صالح.",
                "Veuillez saisir une adresse e-mail valide."
            )
        }
        if (pass.length < 6) {
            return AuthResult.Error(
                "Password must be at least 6 characters.",
                "كلمة المرور يجب أن تكون 6 أحرف على الأقل.",
                "Le mot de passe doit comporter au moins 6 caractères."
            )
        }

        val name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
        val role = if (email.startsWith("admin")) "admin" else if (email.startsWith("business")) "business" else "user"

        val user = AuthUser(
            id = "usr_${hashString(email).take(8)}",
            email = email.trim(),
            displayName = name,
            username = email.substringBefore("@").lowercase().replace(".", "_"),
            role = role
        )

        saveSession(user)
        _currentUser.value = user
        return AuthResult.Success(user)
    }

    fun register(email: String, pass: String, displayName: String, username: String): AuthResult {
        if (displayName.isBlank()) {
            return AuthResult.Error(
                "Please enter your name.",
                "يرجى إدخال اسمك.",
                "Veuillez saisir votre nom."
            )
        }
        if (email.isBlank() || !email.contains("@")) {
            return AuthResult.Error(
                "Please enter a valid email.",
                "يرجى إدخال بريد إلكتروني صحيح.",
                "Veuillez entrer un e-mail valide."
            )
        }
        if (pass.length < 6) {
            return AuthResult.Error(
                "Password must be at least 6 characters.",
                "كلمة المرور يجب أن تتكون من 6 خانات على الأقل.",
                "Le mot de passe doit comporter au moins 6 caractères."
            )
        }

        val role = if (email.startsWith("admin")) "admin" else "user"
        val user = AuthUser(
            id = "usr_${System.currentTimeMillis()}",
            email = email.trim(),
            displayName = displayName.trim(),
            username = username.ifBlank { email.substringBefore("@") },
            role = role
        )

        saveSession(user)
        _currentUser.value = user
        return AuthResult.Success(user)
    }

    fun resetPassword(email: String): Boolean {
        return email.isNotBlank() && email.contains("@")
    }

    fun logout() {
        prefs.edit().clear().apply()
        _currentUser.value = null
    }

    fun deleteAccount(): Boolean {
        prefs.edit().clear().apply()
        _currentUser.value = null
        return true
    }

    private fun saveSession(user: AuthUser) {
        prefs.edit()
            .putString("user_id", user.id)
            .putString("user_email", user.email)
            .putString("user_name", user.displayName)
            .putString("user_username", user.username)
            .putString("user_role", user.role)
            .apply()
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
