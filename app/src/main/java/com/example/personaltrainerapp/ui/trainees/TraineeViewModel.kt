package com.example.personaltrainerapp.ui.trainees

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltrainerapp.data.calendar.AvailabilitySlot
import com.example.personaltrainerapp.data.trainees.FakeTraineeRepository
import com.example.personaltrainerapp.data.trainees.TraineeRepository
import com.example.personaltrainerapp.data.user.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TraineeUiState(
    val isLoading: Boolean = true,
    val trainees: List<UserProfile> = emptyList(),
    val myTrainees: List<UserProfile> = emptyList(),
    val selectedTrainee: UserProfile? = null,
    val tempSlot: AvailabilitySlot? = null
)

class TraineeViewModel(
    private val repository: FakeTraineeRepository = FakeTraineeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TraineeUiState())
    val uiState: StateFlow<TraineeUiState> = _uiState

    fun loadTrainees(trainerId: String) {
        viewModelScope.launch {
            val trainees = repository.getAllTrainees()
            val myTrainees = trainees.filter { trainee -> trainee.trainerId == trainerId}
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                myTrainees = myTrainees
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
    fun addTrainee(trainerId: String, trainee: UserProfile) {
        viewModelScope.launch {
            repository.updateTrainer(trainee.uid, trainerId)

            _uiState.value = _uiState.value.copy(
                myTrainees = _uiState.value.myTrainees + trainee
            )
        }
    }

    fun selectTrainee(traineeId:String){
        viewModelScope.launch {
            val trainee = repository.getTraineeById(traineeId)

            _uiState.value = _uiState.value.copy(
                selectedTrainee = trainee
            )
        }
    }
}