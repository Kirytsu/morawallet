package com.example.morawallet.data.repository

import com.example.morawallet.core.util.Resource
import com.example.morawallet.data.firebase.FirestoreRefs
import com.example.morawallet.data.model.Transaction
import com.example.morawallet.data.model.TransactionType
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface TransactionRepository {
    fun observeTransactions(uid: String): Flow<List<Transaction>>
    suspend fun getTransaction(uid: String, txnId: String): Resource<Transaction>
    suspend fun addTransaction(uid: String, txn: Transaction): Resource<String>
    suspend fun updateTransaction(uid: String, old: Transaction, new: Transaction): Resource<Unit>
    suspend fun deleteTransaction(uid: String, txn: Transaction): Resource<Unit>
}

class FirestoreTransactionRepository(
    private val refs: FirestoreRefs,
) : TransactionRepository {

    override fun observeTransactions(uid: String): Flow<List<Transaction>> = callbackFlow {
        val registration = refs.transactions(uid)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val txns = snapshot?.documents?.mapNotNull { it.toObject<Transaction>() } ?: emptyList()
                trySend(txns)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getTransaction(uid: String, txnId: String): Resource<Transaction> = try {
        val txn = refs.transaction(uid, txnId).get().await().toObject<Transaction>()
        if (txn != null) Resource.Success(txn) else Resource.Error("Transaction not found")
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Could not load transaction", e)
    }

    override suspend fun addTransaction(uid: String, txn: Transaction): Resource<String> = try {
        val doc = refs.transactions(uid).document()
        val toSave = txn.copy(id = doc.id, createdAt = System.currentTimeMillis())
        val batch = refs.db.batch()
        batch.set(doc, toSave)
        applyDeltas(uid, batch, balanceEffects(toSave))
        batch.commit().await()
        Resource.Success(doc.id)
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Could not save transaction", e)
    }

    override suspend fun updateTransaction(
        uid: String,
        old: Transaction,
        new: Transaction,
    ): Resource<Unit> = try {
        val batch = refs.db.batch()
        batch.set(refs.transaction(uid, new.id), new)
        // Net balance change = new effects minus old effects, applied once per wallet.
        val deltas = buildMap<String, Double> {
            balanceEffects(new).forEach { (wallet, value) -> merge(wallet, value) { a, b -> a + b } }
            balanceEffects(old).forEach { (wallet, value) -> merge(wallet, -value) { a, b -> a + b } }
        }
        applyDeltas(uid, batch, deltas)
        batch.commit().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Could not update transaction", e)
    }

    override suspend fun deleteTransaction(uid: String, txn: Transaction): Resource<Unit> = try {
        val batch = refs.db.batch()
        batch.delete(refs.transaction(uid, txn.id))
        applyDeltas(uid, batch, balanceEffects(txn).mapValues { -it.value })
        batch.commit().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Could not delete transaction", e)
    }

    /** Signed balance change each wallet receives when [txn] is applied. */
    private fun balanceEffects(txn: Transaction): Map<String, Double> = when (txn.typeEnum) {
        TransactionType.INCOME -> mapOf(txn.walletId to txn.amount)
        TransactionType.EXPENSE -> mapOf(txn.walletId to -txn.amount)
        TransactionType.TRANSFER -> buildMap {
            merge(txn.walletId, -txn.amount) { a, b -> a + b }
            txn.toWalletId?.let { to ->
                merge(to, txn.convertedAmount ?: txn.amount) { a, b -> a + b }
            }
        }
    }

    private fun applyDeltas(
        uid: String,
        batch: com.google.firebase.firestore.WriteBatch,
        deltas: Map<String, Double>,
    ) {
        deltas.forEach { (walletId, delta) ->
            if (walletId.isNotEmpty() && delta != 0.0) {
                batch.update(refs.wallet(uid, walletId), "balance", FieldValue.increment(delta))
            }
        }
    }
}
