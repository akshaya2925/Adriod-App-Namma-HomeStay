package com.nammahomestay.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.nammahomestay.data.Inquiry
import com.nammahomestay.data.Reply
import com.nammahomestay.firebase.FirebaseManager
import com.nammahomestay.utils.AppResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class InquiryRepository {
    fun getInquiriesFlow(): Flow<AppResult<List<Inquiry>>> = callbackFlow {
        trySend(AppResult.Loading)
        val uid = FirebaseManager.auth.currentUser?.uid
        if (uid == null) {
            trySend(AppResult.Error("Not logged in"))
            close()
            return@callbackFlow
        }

        val listener = FirebaseManager.firestore
            .collection("hosts").document(uid)
            .collection("inquiries")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(AppResult.Error(error.localizedMessage ?: "Error"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val inquiries = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Inquiry::class.java)?.apply { id = doc.id }
                    }
                    trySend(AppResult.Success(inquiries))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun markAsRead(inquiryId: String) {
        val uid = FirebaseManager.auth.currentUser?.uid ?: return
        FirebaseManager.firestore
            .collection("hosts").document(uid)
            .collection("inquiries").document(inquiryId)
            .update("isRead", true).await()
    }

    suspend fun sendReply(inquiryId: String, message: String) {
        val uid = FirebaseManager.auth.currentUser?.uid ?: return
        val reply = Reply(message = message, sentAt = Timestamp.now())
        FirebaseManager.firestore
            .collection("hosts").document(uid)
            .collection("inquiries").document(inquiryId)
            .collection("replies").add(reply).await()
    }
}
