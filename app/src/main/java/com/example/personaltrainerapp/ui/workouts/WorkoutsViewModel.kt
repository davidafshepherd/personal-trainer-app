package com.example.personaltrainerapp.ui.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltrainerapp.data.workouts.RealWorkoutsRepository
//import com.example.personaltrainerapp.data.workouts.FakeWorkoutsRepository
import com.example.personaltrainerapp.data.workouts.Workout
import com.example.personaltrainerapp.data.workouts.WorkoutsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class WorkoutsUiState(
    val isLoading: Boolean = true,
    val workouts: List<Workout> = emptyList(),
    val selectedWorkout: Workout? = null,
    val error: String? = null
)

class WorkoutsViewModel(
    private val repository: WorkoutsRepository = RealWorkoutsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutsUiState())
    val uiState: StateFlow<WorkoutsUiState> = _uiState

    fun loadWorkouts(traineeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val workouts = repository.getUpcomingWorkouts(traineeId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    workouts = workouts,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load workouts"
                )
            }
        }
    }

   fun loadAllWorkouts(){
       viewModelScope.launch {
           _uiState.value = _uiState.value.copy(isLoading = true)
           val workouts = repository.getAllWorkouts()
           _uiState.value = _uiState.value.copy(
               isLoading = false,
               workouts = workouts,
               error = null
           )
       }
   }

    fun selectWorkout(workout: Workout) {
        _uiState.value = _uiState.value.copy(
            selectedWorkout = workout
        )
    }

    fun clearSelectedWorkout() {
        _uiState.value = _uiState.value.copy(
            selectedWorkout = null
        )
    }

    fun toggleExerciseComplete(
        workoutId: String,
        exerciseId: String
    ) {
        val currentState = _uiState.value

        val workout = currentState.workouts.firstOrNull { it.id == workoutId }
            ?: return

        val exercise = workout.exercises.firstOrNull { it.id == exerciseId }
            ?: return

        val newCompleteValue = !exercise.completed

        viewModelScope.launch {
            repository.markExerciseComplete(
                workoutId = workoutId,
                exerciseId = exerciseId,
                completed = newCompleteValue
            )

            val updatedWorkouts = currentState.workouts.map { existingWorkout ->
                if (existingWorkout.id != workoutId) {
                    existingWorkout
                } else {
                    existingWorkout.copy(
                        exercises = existingWorkout.exercises.map { existingExercise ->
                            if (existingExercise.id == exerciseId) {
                                existingExercise.copy(completed = newCompleteValue)
                            } else {
                                existingExercise
                            }
                        }
                    )
                }
            }

            val updatedSelectedWorkout = updatedWorkouts.firstOrNull {
                it.id == currentState.selectedWorkout?.id
            }

            _uiState.value = currentState.copy(
                workouts = updatedWorkouts,
                selectedWorkout = updatedSelectedWorkout
            )
        }
    }
}