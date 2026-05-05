package com.example.personaltrainerapp.ui.trainer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltrainerapp.data.trainer.TrainerRepository
import com.example.personaltrainerapp.data.user.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TrainerUiState(
    val isLoading: Boolean = true,
    val trainers: List<UserProfile> = emptyList(),
    val selectedTrainer: UserProfile? = null,
    val pendingRequestedTrainerIds: Set<String> = emptySet()
)

class TrainerViewModel(
    private val repository: TrainerRepository = TrainerRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainerUiState())
    val uiState: StateFlow<TrainerUiState> = _uiState

    fun loadTrainers() {
        viewModelScope.launch {
            val trainers = repository.getAllTrainers()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                trainers = trainers
            )
        }
    }

    fun requestTrainer(traineeId: String, trainer: UserProfile) {
        viewModelScope.launch {
            repository.requestTrainer(traineeId, trainer.uid)

            _uiState.value = _uiState.value.copy(
                pendingRequestedTrainerIds =
                    _uiState.value.pendingRequestedTrainerIds + trainer.uid
            )
        }
    }

    fun loadAssignedTrainer(traineeId: String) {
        viewModelScope.launch {
            val trainer = repository.getAssignedTrainer(traineeId)

            _uiState.value = _uiState.value.copy(
                selectedTrainer = trainer
            )
        }
    }

    fun unenrolFromTrainer(traineeId: String) {
        viewModelScope.launch {
            repository.unenrolFromTrainer(traineeId)

            _uiState.value = _uiState.value.copy(
                selectedTrainer = null
            )
        }
    }

    fun checkPendingRequest(
        traineeId: String,
        trainerId: String
    ) {
        viewModelScope.launch {
            val hasPendingRequest = repository.hasPendingTrainerRequest(
                traineeId = traineeId,
                trainerId = trainerId
            )

            if (hasPendingRequest) {
                _uiState.value = _uiState.value.copy(
                    pendingRequestedTrainerIds =
                        _uiState.value.pendingRequestedTrainerIds + trainerId
                )
            }
        }
    }
}