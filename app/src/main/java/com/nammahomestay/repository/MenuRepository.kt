package com.nammahomestay.repository

import com.google.firebase.firestore.SetOptions
import com.nammahomestay.data.DailyMenu
import com.nammahomestay.firebase.FirebaseManager
import com.nammahomestay.utils.AppResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MenuRepository {
    fun getTodayMenuFlow(dateStr: String): Flow<AppResult<DailyMenu>> = callbackFlow {
        trySend(AppResult.Loading)
        val uid = FirebaseManager.auth.currentUser?.uid
        if (uid == null) {
            trySend(AppResult.Error("Not logged in"))
            close()
            return@callbackFlow
        }

        val listener = FirebaseManager.firestore
            .collection("hosts").document(uid)
            .collection("dailyMenu").document(dateStr)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(AppResult.Error(error.localizedMessage ?: "Error"))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val menu = snapshot.toObject(DailyMenu::class.java)
                    if (menu != null) trySend(AppResult.Success(menu))
                } else {
                    trySend(AppResult.Success(DailyMenu(date = dateStr)))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveMenu(menu: DailyMenu, dateStr: String) {
        val uid = FirebaseManager.auth.currentUser?.uid ?: return
        FirebaseManager.firestore
            .collection("hosts").document(uid)
            .collection("dailyMenu").document(dateStr)
            .set(menu, SetOptions.merge()).await()
    }
}
