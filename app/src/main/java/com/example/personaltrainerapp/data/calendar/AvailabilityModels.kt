package com.example.personaltrainerapp.data.calendar

import java.time.LocalDate

enum class AvailabilityStatus {
    IDEAL,
    MAYBE,
    BUSY
}

data class AvailabilitySlot(
    val id: String = "",
    val traineeId: String = "",
    val date: LocalDate = LocalDate.now(),
    val hour: Int = 0,
    val status: AvailabilityStatus = AvailabilityStatus.IDEAL
)