package com.nammahomestay.utils

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SeedData {

    fun seedAll() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) return

        CoroutineScope(Dispatchers.IO).launch {
            val db = Firebase.firestore
            val root = db.collection("hosts").document(userId)

            // ── 1. HOST PROFILE ──────────────────────────────
            root.set(
                hashMapOf(
                    "homestayName"         to "Lakshmi Amma's HomeStay",
                    "village"              to "Udupi, Karnataka",
                    "numRooms"             to 3,
                    "maxGuests"            to 6,
                    "roomType"             to "Private Room",
                    "description"          to "Peaceful coastal home surrounded by paddy fields. Authentic home-cooked Udupi cuisine daily.",
                    "photoUrls"            to listOf<String>(),
                    "verificationChecklist" to mapOf(
                        "cleanBedsheets"   to true,
                        "workingFan"       to true,
                        "cleanBathroom"    to true,
                        "mosquitoNet"      to false,
                        "drinkingWater"    to true,
                        "chargingPoints"   to false
                    ),
                    "whatsapp"             to "+91 94801 23456",
                    "languages"            to listOf("Kannada", "English", "Hindi"),
                    "isProfileComplete"    to true,
                    "isAcceptingGuests"    to true
                )
            )

            // ── 2. DAILY MENU ────────────────────────────────
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            root.collection("dailyMenu").document(today).set(
                hashMapOf(
                    "date"           to today,
                    "description"    to "Akki Rotti, Bamboo Shoot Curry, and Fish Curry. Fresh local catch!",
                    "mealsAvailable" to listOf("Breakfast", "Lunch", "Dinner"),
                    "lastUpdated"    to Timestamp.now()
                )
            )

            // ── 3. INQUIRIES ─────────────────────────────────
            root.collection("inquiries").add(
                hashMapOf(
                    "travelerName"  to "Priya Sharma",
                    "travelerPhone" to "+91 98400 11234",
                    "message"       to "Namaskara! Is your room available next weekend? We are a family of 3 looking for a 2-night stay.",
                    "isRead"        to false,
                    "createdAt"     to Timestamp(Date(System.currentTimeMillis() - 3_600_000L))
                )
            )
            root.collection("inquiries").add(
                hashMapOf(
                    "travelerName"  to "Rajan M.",
                    "travelerPhone" to "+91 87654 99001",
                    "message"       to "Do you serve Bamboo shoot curry? We are 4 people planning to visit on Saturday.",
                    "isRead"        to false,
                    "createdAt"     to Timestamp(Date(System.currentTimeMillis() - 7_200_000L))
                )
            )

            // ── 4. LOCAL SPOTS ────────────────────────────────
            val spots = listOf(
                hashMapOf(
                    "name"        to "Unchalli Falls",
                    "category"    to "Nature",
                    "description" to "Hidden waterfall in Western Ghats. Best after monsoon.",
                    "distance"    to "38 km",
                    "bestTime"    to "Post-monsoon",
                    "locationUrl" to "https://maps.app.goo.gl/9ZpLz4ZzJ5zR7Z7v9",
                    "entryFee"    to "₹20 per person",
                    "timings"     to "8:00 AM - 5:00 PM",
                    "createdAt"   to Timestamp.now()
                ),
                hashMapOf(
                    "name"        to "Maravanthe Beach",
                    "category"    to "Beach",
                    "description" to "Sea on one side, river on the other. Best at sunset.",
                    "distance"    to "22 km",
                    "bestTime"    to "Sunset",
                    "locationUrl" to "https://maps.app.goo.gl/r6z9P4Y4Q4X4W4V4",
                    "entryFee"    to "Free",
                    "timings"     to "Open 24 hours",
                    "createdAt"   to Timestamp.now()
                ),
                hashMapOf(
                    "name"        to "Organic Paddy Farm Walk",
                    "category"    to "Experience",
                    "description" to "Our own paddy fields. Sunrise walk loved by every guest.",
                    "distance"    to "0 km",
                    "bestTime"    to "Sunrise",
                    "locationUrl" to "",
                    "entryFee"    to "Complimentary for guests",
                    "timings"     to "Best at 6:00 AM",
                    "createdAt"   to Timestamp.now()
                )
            )

            for (spot in spots) {
                root.collection("localSpots").add(spot)
            }
        }
    }

    fun migrateLanguages() {
        val db = Firebase.firestore
        db.collection("hosts").get().addOnSuccessListener { snapshot ->
            for (doc in snapshot.documents) {
                val languages = doc.get("languages")
                if (languages is String) {
                    val fixed = languages.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    doc.reference.update("languages", fixed)
                }
            }
        }
    }
}
