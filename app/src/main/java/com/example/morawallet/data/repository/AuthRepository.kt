package com.example.morawallet.data.repository

import com.example.morawallet.core.util.Resource
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    val isLoggedIn: Boolean
    fun currentUserId(): String?
    fun currentEmail(): String?

    /** Creates the auth account and returns the new uid. */
    suspend fun register(email: String, password: String): Resource<String>
    suspend fun login(email: String, password: String): Resource<Unit>
    fun logout()
    suspend fun changePassword(currentPassword: String, newPassword: String): Resource<Unit>
}

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
) : AuthRepository {

    override val isLoggedIn: Boolean get() = auth.currentUser != null
    override fun currentUserId(): String? = auth.currentUser?.uid
    override fun currentEmail(): String? = auth.currentUser?.email

    override suspend fun register(email: String, password: String): Resource<String> = try {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val uid = result.user?.uid
        if (uid != null) Resource.Success(uid) else Resource.Error("Registration failed")
    } catch (e: Exception) {
        Resource.Error(e.toAuthMessage(), e)
    }

    override suspend fun login(email: String, password: String): Resource<Unit> = try {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.toAuthMessage(), e)
    }

    override fun logout() = auth.signOut()

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
    ): Resource<Unit> = try {
        val user = auth.currentUser
        val email = user?.email
        when {
            user == null -> Resource.Error("You are not signed in")
            email == null -> Resource.Error("No email on this account")
            else -> {
                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                user.reauthenticate(credential).await()
                user.updatePassword(newPassword).await()
                Resource.Success(Unit)
            }
        }
    } catch (e: Exception) {
        Resource.Error(e.toAuthMessage(), e)
    }
}

internal fun Exception.toAuthMessage(): String = when (this) {
    is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password"
    is FirebaseAuthUserCollisionException -> "An account with this email already exists"
    is FirebaseAuthWeakPasswordException -> reason ?: "Password is too weak"
    is FirebaseAuthInvalidUserException -> "No account found for this email"
    is FirebaseNetworkException -> "Network error. Check your connection."
    else -> localizedMessage ?: "Something went wrong. Please try again."
}
