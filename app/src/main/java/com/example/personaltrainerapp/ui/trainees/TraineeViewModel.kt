package com.example.personaltrainerapp.ui.trainees

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltrainerapp.data.calendar.AvailabilitySlot
import com.example.personaltrainerapp.data.trainees.TraineeRepository
import com.example.personaltrainerapp.data.trainer.TrainerRequest
import com.example.personaltrainerapp.data.user.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TraineeUiState(
    val isLoading: Boolean = true,
    val allTrainees: List<UserProfile> = emptyList(),
    val myTrainees: List<UserProfile> = emptyList(),
    val myRequests: List<TrainerRequest> = emptyList(),
    val selectedTrainee: UserProfile? = null,
    val tempSlot: AvailabilitySlot? = null,
    val availability: Int = 0
)

class TraineeViewModel(
    private val repository: TraineeRepository = TraineeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TraineeUiState())
    val uiState: StateFlow<TraineeUiState> = _uiState

    fun loadTrainees(trainerId: String) {
        viewModelScope.launch {
            val trainees = repository.getAllTrainees()
            val myTrainees = trainees.filter { trainee -> trainee.trainerId == trainerId}
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                myTrainees = myTrainees,
                allTrainees = trainees
            )
        }
    }

    fun setTempSlot(traineeId: String, date: LocalDate, hour:Int){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                tempSlot = AvailabilitySlot(traineeId = traineeId, date = date, hour = hour)
            )
        }
    }

    fun assignTrainee(trainerId: String, traineeId: String) {
        viewModelScope.launch {
            repository.updateTrainer(traineeId, trainerId)
        }
    }

    fun updateRequestStatus(requestId: String, status: String){
        viewModelScope.launch {
            repository.updateRequestStatus(requestId, status)
        }
    }
    fun setTrainerAvailability(trainerId: String, availability: Int){
        viewModelScope.launch {
            repository.setTrainerAvailability(trainerId, availability)
        }
    }

    fun loadRequests(trainerId: String){
        viewModelScope.launch {
            val requests = repository.getRequests(trainerId)
            _uiState.value = _uiState.value.copy(
                myRequests = requests
            )
        }
    }

    fun loadAvailability(trainerId: String){
        viewModelScope.launch {
            val availability = repository.getTrainerAvailability(trainerId)

            _uiState.value = _uiState.value.copy(
                availability = availability
            )
        }
    }

    fun selectTrainee(traineeId:String){
        viewModelScope.launch {
            val trainee = repository.getTraineeById(traineeId)

            _uiState.value = _uiState.value.copy(
                selectedTrainee = trainee,
                isLoading = false
            )
        }
    }
}