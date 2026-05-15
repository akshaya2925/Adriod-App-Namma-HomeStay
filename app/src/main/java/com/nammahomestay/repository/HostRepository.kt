package com.nammahomestay.repository

import com.nammahomestay.data.HostProfile
import com.nammahomestay.firebase.FirebaseManager
import com.nammahomestay.utils.AppResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class HostRepository {
    fun getProfileFlow(): Flow<AppResult<HostProfile>> = callbackFlow {
        trySend(AppResult.Loading)
        val uid = FirebaseManager.auth.currentUser?.uid
        if (uid == null) {
            trySend(AppResult.Error("Not logged in"))
            close()
            return@callbackFlow
        }

        val listener = FirebaseManager.firestore.collection("hosts").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(AppResult.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val profile = snapshot.toObject(HostProfile::class.java)
                    if (profile != null) {
                        trySend(AppResult.Success(profile))
                    }
                } else {
                    trySend(AppResult.Success(HostProfile()))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveProfile(profile: HostProfile) {
        val uid = FirebaseManager.auth.currentUser?.uid ?: return
        FirebaseManager.firestore.collection("hosts").document(uid).update(
            mapOf(
                "homestayName" to profile.homestayName,
                "village" to profile.village,
                "numRooms" to profile.numRooms,
                "maxGuests" to profile.maxGuests,
                "roomType" to profile.roomType,
                "description" to profile.description,
                "photoUrls" to profile.photoUrls,
                "verificationChecklist" to profile.verificationChecklist,
                "whatsapp" to profile.whatsapp,
                "languages" to profile.languages,
                "isProfileComplete" to true
            )
        ).await()
    }
}
