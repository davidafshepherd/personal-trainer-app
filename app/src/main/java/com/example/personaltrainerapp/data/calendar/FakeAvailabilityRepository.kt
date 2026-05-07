package com.example.personaltrainerapp.data.calendar

import java.time.LocalDate

class FakeAvailabilityRepository {

    private var slots = emptyList<AvailabilitySlot>()

    suspend fun getAvailabilitySlots(
        traineeId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AvailabilitySlot> {
        return slots.filter { slot ->
            slot.traineeId == traineeId &&
                    !slot.date.isBefore(startDate) &&
                    !slot.date.isAfter(endDate)
        }
    }

     suspend fun saveAvailabilitySlot(slot: AvailabilitySlot) {
        slots = slots.filterNot { existing ->
            existing.traineeId == slot.traineeId &&
                    existing.date == slot.date &&
                    existing.hour == slot.hour
        } + slot
    }
}