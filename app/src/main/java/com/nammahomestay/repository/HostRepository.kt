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
                    val data = snapshot.data
                    if (data != null) {
                        val profile = HostProfile(
                            homestayName = data["homestayName"] as? String ?: "",
                            village = data["village"] as? String ?: "",
                            numRooms = (data["numRooms"] as? Long)?.toInt() ?: 0,
                            maxGuests = (data["maxGuests"] as? Long)?.toInt() ?: 0,
                            roomType = data["roomType"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            photoUrls = (data["photoUrls"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            verificationChecklist = (data["verificationChecklist"] as? Map<*, *>)?.entries?.associate {
                                it.key.toString() to (it.value as? Boolean ?: false)
                            } ?: emptyMap(),
                            whatsapp = data["whatsapp"] as? String ?: "",
                            languages = parseLanguages(data["languages"]),
                            isProfileComplete = data["isProfileComplete"] as? Boolean ?: false,
                        )
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

    private fun parseLanguages(value: Any?): List<String> {
        return when (value) {
            is List<*> -> value.filterIsInstance<String>()
            is String -> value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            else -> emptyList()
        }
    }
}
