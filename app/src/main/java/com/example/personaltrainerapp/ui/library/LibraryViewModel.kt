package com.example.personaltrainerapp.ui.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltrainerapp.data.calendar.AvailabilitySlot
import com.example.personaltrainerapp.data.workouts.Exercise
import com.example.personaltrainerapp.data.exercises.ExerciseRepository
import com.example.personaltrainerapp.data.workouts.RealWorkoutsRepository
import com.example.personaltrainerapp.data.workouts.Workout
import com.example.personaltrainerapp.data.workouts.WorkoutsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LibraryUiState(
    val isLoading: Boolean = true,
    val workouts: List<Workout> = emptyList(),
    val exercises: List<Exercise> = emptyList(),
    var selectedWorkout: Workout? = null,
    var selectedExercise: Exercise? = null
)

class LibraryViewModel(
    private val wRepository: RealWorkoutsRepository = RealWorkoutsRepository(),
    private val eRepository: ExerciseRepository = ExerciseRepository()

) : ViewModel() {
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState

    fun loadAll(trainerId: String) {
        viewModelScope.launch {
            val workouts = wRepository.getTrainersWorkouts(trainerId)

            val exercises = eRepository.getAllExercises(trainerId)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                workouts = workouts,
                exercises = exercises,
            )
        }
    }

    fun selectWorkout(workout: Workout) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedWorkout = workout
            )
        }
    }

    fun selectExercise(exercise: Exercise) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedExercise = exercise
            )
        }
    }

    fun saveExercise(exercise: Exercise) {
        viewModelScope.launch {
            eRepository.saveNewExercise(exercise)
        }

    }

    fun saveWorkout(workout: Workout) {
        viewModelScope.launch {
            wRepository.saveNewWorkout(workout)
        }
    }

    fun scheduleWorkout(
        workout: Workout,
        traineeId: String,
        trainerId: String,
        slot: AvailabilitySlot
    ) {
        viewModelScope.launch {
            wRepository.scheduleWorkout(
                workout,
                traineeId,
                slot
            )
        }
        loadAll(trainerId)
    }
}