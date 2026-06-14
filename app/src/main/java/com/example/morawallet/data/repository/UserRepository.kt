package com.example.morawallet.data.repository

import com.example.morawallet.core.util.Resource
import com.example.morawallet.data.firebase.FirestoreRefs
import com.example.morawallet.data.model.User
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface UserRepository {
    suspend fun createUserProfile(
        uid: String,
        email: String,
        displayName: String,
        baseCurrency: String = "USD",
    ): Resource<Unit>

    suspend fun getUser(uid: String): Resource<User>
    suspend fun updateBaseCurrency(uid: String, currencyCode: String): Resource<Unit>
    fun observeUser(uid: String): Flow<User?>
}

class FirestoreUserRepository(
    private val refs: FirestoreRefs,
) : UserRepository {

    override suspend fun createUserProfile(
        uid: String,
        email: String,
        displayName: String,
        baseCurrency: String,
    ): Resource<Unit> = try {
        val user = User(
            uid = uid,
            email = email,
            displayName = displayName,
            baseCurrency = baseCurrency,
            createdAt = System.currentTimeMillis(),
        )
        refs.user(uid).set(user).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Could not save profile", e)
    }

    override suspend fun getUser(uid: String): Resource<User> = try {
        val snapshot = refs.user(uid).get().await()
        val user = snapshot.toObject<User>()
        if (user != null) Resource.Success(user) else Resource.Error("Profile not found")
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Could not load profile", e)
    }

    override suspend fun updateBaseCurrency(uid: String, currencyCode: String): Resource<Unit> =
        try {
            // set+merge (not update) so it still works if the profile doc doesn't exist yet.
            refs.user(uid).set(mapOf("baseCurrency" to currencyCode), SetOptions.merge()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Could not update currency", e)
        }

    override fun observeUser(uid: String): Flow<User?> = callbackFlow {
        val registration = refs.user(uid).addSnapshotListener { snapshot, _ ->
            trySend(snapshot?.toObject<User>())
        }
        awaitClose { registration.remove() }
    }
}
