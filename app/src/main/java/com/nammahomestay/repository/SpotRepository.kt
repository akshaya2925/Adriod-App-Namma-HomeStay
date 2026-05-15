package com.nammahomestay.repository

import com.nammahomestay.data.LocalSpot
import com.nammahomestay.firebase.FirebaseManager
import com.nammahomestay.utils.AppResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SpotRepository {
    fun getSpotsFlow(): Flow<AppResult<List<LocalSpot>>> = callbackFlow {
        trySend(AppResult.Loading)
        val uid = FirebaseManager.auth.currentUser?.uid
        if (uid == null) {
            trySend(AppResult.Error("Not logged in"))
            close()
            return@callbackFlow
        }

        val listener = FirebaseManager.firestore
            .collection("hosts").document(uid)
            .collection("localSpots")
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(AppResult.Error(error.localizedMessage ?: "Error"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val spots = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(LocalSpot::class.java)?.apply { id = doc.id }
                    }
                    trySend(AppResult.Success(spots))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addSpot(spot: LocalSpot) {
        val uid = FirebaseManager.auth.currentUser?.uid ?: return
        FirebaseManager.firestore
            .collection("hosts").document(uid)
            .collection("localSpots").document(spot.id)
            .set(spot).await()
    }

    suspend fun deleteSpot(spotId: String) {
        val uid = FirebaseManager.auth.currentUser?.uid ?: return
        FirebaseManager.firestore
            .collection("hosts").document(uid)
            .collection("localSpots").document(spotId)
            .delete().await()
    }
}
