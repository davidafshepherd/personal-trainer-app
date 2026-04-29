package com.example.personaltrainerapp.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltrainerapp.data.calendar.AvailabilityRepository
import com.example.personaltrainerapp.data.calendar.AvailabilitySlot
import com.example.personaltrainerapp.data.calendar.AvailabilityStatus
import com.example.personaltrainerapp.data.calendar.FakeAvailabilityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CalendarUiState(
    val isLoading: Boolean = true,
    val slots: Map<String, AvailabilityStatus> = emptyMap(),
    val error: String? = null
)

class CalendarViewModel(
    private val repository: AvailabilityRepository = FakeAvailabilityRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState

    fun loadAvailability(
        traineeId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val slots = repository.getAvailabilitySlots(
                    traineeId = traineeId,
                    startDate = startDate,
                    endDate = endDate
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    slots = slots.associate { slot ->
                        availabilityKey(slot.date, slot.hour) to slot.status
                    },
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load availability"
                )
            }
        }
    }

    fun cycleAvailability(
        traineeId: String,
        date: LocalDate,
        hour: Int
    ) {
        val key = availabilityKey(date, hour)
        val currentStatus = _uiState.value.slots[key] ?: AvailabilityStatus.IDEAL
        val nextStatus = nextAvailabilityStatus(currentStatus)

        val updatedSlots = _uiState.value.slots.toMutableMap().also {
            it[key] = nextStatus
        }

        _uiState.value = _uiState.value.copy(
            slots = updatedSlots
        )

        viewModelScope.launch {
            repository.saveAvailabilitySlot(
                AvailabilitySlot(
                    id = "${traineeId}_${date}_$hour",
                    traineeId = traineeId,
                    date = date,
                    hour = hour,
                    status = nextStatus
                )
            )
        }
    }

    private fun nextAvailabilityStatus(
        current: AvailabilityStatus
    ): AvailabilityStatus {
        return when (current) {
            AvailabilityStatus.IDEAL -> AvailabilityStatus.MAYBE
            AvailabilityStatus.MAYBE -> AvailabilityStatus.BUSY
            AvailabilityStatus.BUSY -> AvailabilityStatus.IDEAL
        }
    }
}

fun availabilityKey(date: LocalDate, hour: Int): String {
    return "${date}_$hour"
}