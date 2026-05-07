package com.example.personaltrainerapp.data.calendar

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class AvailabilityRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getAvailabilitySlots(
        traineeId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AvailabilitySlot> {
        return try {
            val snapshot = db.collection("availabilitySlots")
                .whereEqualTo("traineeId", traineeId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val dateString = doc.getString("date") ?: return@mapNotNull null
                val date = LocalDate.parse(dateString)

                if (date.isBefore(startDate) || date.isAfter(endDate)) {
                    return@mapNotNull null
                }

                AvailabilitySlot(
                    id = doc.id,
                    traineeId = doc.getString("traineeId") ?: "",
                    date = date,
                    hour = doc.getLong("hour")?.toInt() ?: 0,
                    status = runCatching {
                        AvailabilityStatus.valueOf(
                            doc.getString("status") ?: AvailabilityStatus.IDEAL.name
                        )
                    }.getOrDefault(AvailabilityStatus.IDEAL)
                )
            }
        } catch (e: Exception) {
            Log.e("AvailabilityRepository", "Failed to load availability", e)
            emptyList()
        }
    }

    suspend fun saveAvailabilitySlot(slot: AvailabilitySlot) {
        try {
            val slotId = "${slot.traineeId}_${slot.date}_${slot.hour}"

            db.collection("availabilitySlots")
                .document(slotId)
                .set(
                    mapOf(
                        "id" to slotId,
                        "traineeId" to slot.traineeId,
                        "date" to slot.date.toString(),
                        "hour" to slot.hour,
                        "status" to slot.status.name
                    )
                )
                .await()
        } catch (e: Exception) {
            Log.e("AvailabilityRepository", "Failed to save availability", e)
        }
    }
}