package com.example.morawallet.data.repository

import com.example.morawallet.core.util.Resource
import com.example.morawallet.data.firebase.FirestoreRefs
import com.example.morawallet.data.model.Wallet
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface WalletRepository {
    fun observeWallets(uid: String): Flow<List<Wallet>>
    suspend fun getWallet(uid: String, walletId: String): Resource<Wallet>
    suspend fun createWallet(uid: String, wallet: Wallet): Resource<String>
    suspend fun updateWallet(uid: String, wallet: Wallet): Resource<Unit>
    suspend fun deleteWallet(uid: String, walletId: String): Resource<Unit>
}

class FirestoreWalletRepository(
    private val refs: FirestoreRefs,
) : WalletRepository {

    override fun observeWallets(uid: String): Flow<List<Wallet>> = callbackFlow {
        val registration = refs.wallets(uid)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val wallets = snapshot?.documents?.mapNotNull { it.toObject<Wallet>() } ?: emptyList()
                trySend(wallets)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getWallet(uid: String, walletId: String): Resource<Wallet> = try {
        val wallet = refs.wallet(uid, walletId).get().await().toObject<Wallet>()
        if (wallet != null) Resource.Success(wallet) else Resource.Error("Wallet not found")
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Could not load wallet", e)
    }

    override suspend fun createWallet(uid: String, wallet: Wallet): Resource<String> = try {
        val doc = refs.wallets(uid).document()
        val toSave = wallet.copy(id = doc.id, createdAt = System.currentTimeMillis())
        doc.set(toSave).await()
        Resource.Success(doc.id)
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Could not create wallet", e)
    }

    override suspend fun updateWallet(uid: String, wallet: Wallet): Resource<Unit> = try {
        refs.wallet(uid, wallet.id).set(wallet).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Could not update wallet", e)
    }

    /** Deletes the wallet and cascade-deletes every transaction referencing it. */
    override suspend fun deleteWallet(uid: String, walletId: String): Resource<Unit> = try {
        val asSource = refs.transactions(uid).whereEqualTo("walletId", walletId).get().await()
        val asDestination = refs.transactions(uid).whereEqualTo("toWalletId", walletId).get().await()
        val batch = refs.db.batch()
        (asSource.documents + asDestination.documents)
            .distinctBy { it.id }
            .forEach { batch.delete(it.reference) }
        batch.delete(refs.wallet(uid, walletId))
        batch.commit().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Could not delete wallet", e)
    }
}
