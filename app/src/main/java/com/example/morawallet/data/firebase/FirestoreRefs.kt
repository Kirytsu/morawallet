package com.example.morawallet.data.firebase

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

/** Central place for Firestore collection/document paths (per-user data). */
class FirestoreRefs(val db: FirebaseFirestore) {

    fun users(): CollectionReference = db.collection("users")

    fun user(uid: String): DocumentReference = users().document(uid)

    fun wallets(uid: String): CollectionReference = user(uid).collection("wallets")

    fun wallet(uid: String, walletId: String): DocumentReference = wallets(uid).document(walletId)

    fun transactions(uid: String): CollectionReference = user(uid).collection("transactions")

    fun transaction(uid: String, txnId: String): DocumentReference =
        transactions(uid).document(txnId)
}
