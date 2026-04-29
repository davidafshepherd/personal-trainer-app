package com.example.personaltrainerapp.data.calendar

import java.time.LocalDate

interface AvailabilityRepository {
    suspend fun getAvailabilitySlots(
        traineeId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AvailabilitySlot>

    suspend fun saveAvailabilitySlot(slot: AvailabilitySlot)
}