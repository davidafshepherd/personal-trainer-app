package com.example.personaltrainerapp.data.notifications

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class NotificationRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getPreferences(userId: String): NotificationPreferences {
        return try {
            val doc = db.collection("users")
                .document(userId)
                .get()
                .await()

            val map = doc.get("notificationPreferences") as? Map<*, *>

            NotificationPreferences(
                emailWorkoutScheduled = map?.get("emailWorkoutScheduled") as? Boolean ?: true,
                pushWorkoutScheduled = map?.get("pushWorkoutScheduled") as? Boolean ?: true
            )
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Failed to load notification preferences", e)
            NotificationPreferences()
        }
    }

    suspend fun savePreferences(
        userId: String,
        preferences: NotificationPreferences
    ) {
        try {
            db.collection("users")
                .document(userId)
                .set(
                    mapOf(
                        "notificationPreferences" to mapOf(
                            "emailWorkoutScheduled" to preferences.emailWorkoutScheduled,
                            "pushWorkoutScheduled" to preferences.pushWorkoutScheduled
                        )
                    ),
                    SetOptions.merge()
                )
                .await()
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Failed to save notification preferences", e)
        }
    }

    suspend fun saveFcmToken(userId: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()

            db.collection("users")
                .document(userId)
                .set(
                    mapOf("fcmToken" to token),
                    SetOptions.merge()
                )
                .await()
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Failed to save FCM token", e)
        }
    }

    suspend fun createWorkoutScheduledNotifications(
        traineeId: String,
        workoutName: String
    ) {
        try {
            val traineeDoc = db.collection("users")
                .document(traineeId)
                .get()
                .await()

            val traineeEmail = traineeDoc.getString("email") ?: ""
            val prefs = traineeDoc.get("notificationPreferences") as? Map<*, *>

            val emailEnabled =
                prefs?.get("emailWorkoutScheduled") as? Boolean ?: true

            val pushEnabled =
                prefs?.get("pushWorkoutScheduled") as? Boolean ?: true

            if (emailEnabled && traineeEmail.isNotBlank()) {
                db.collection("mail")
                    .add(
                        mapOf(
                            "to" to listOf(traineeEmail),
                            "message" to mapOf(
                                "subject" to "Workout scheduled",
                                "text" to "Your trainer has scheduled a new workout: $workoutName."
                            )
                        )
                    )
                    .await()
            }

            if (pushEnabled) {
                db.collection("notificationJobs")
                    .add(
                        mapOf(
                            "traineeId" to traineeId,
                            "type" to "WORKOUT_SCHEDULED",
                            "title" to "Workout scheduled",
                            "body" to "Your trainer has scheduled a new workout: $workoutName.",
                            "workoutName" to workoutName,
                            "createdAt" to System.currentTimeMillis()
                        )
                    )
                    .await()
            }
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Failed to create workout notifications", e)
        }
    }
}