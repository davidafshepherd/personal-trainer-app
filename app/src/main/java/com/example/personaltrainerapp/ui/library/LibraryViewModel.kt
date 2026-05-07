package com.example.personaltrainerapp.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltrainerapp.data.calendar.AvailabilitySlot
import com.example.personaltrainerapp.data.workouts.Exercise
import com.example.personaltrainerapp.data.workouts.FakeWorkoutsRepository
import com.example.personaltrainerapp.data.exercises.FakeExerciseRepository
import com.example.personaltrainerapp.data.exercises.ExerciseRepository
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
    var selectedExercise: Exercise? = null,
    var creatingWorkout: Boolean = false,
    var creatingExercise: Boolean = false
)

class LibraryViewModel(
    private val wRepository: WorkoutsRepository = FakeWorkoutsRepository(),
    private val eRepository: FakeExerciseRepository = FakeExerciseRepository()

) : ViewModel() {
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState

    fun loadAll() {
        viewModelScope.launch {
            val workouts = wRepository.getAllWorkouts()
            val exercises = eRepository.getAllExercises()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                workouts = workouts,
                exercises = exercises
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

    }

    fun saveWorkout(workout: Workout) {

    }

    fun scheduleWorkout(
        workout: Workout,
        traineeId: String,
        slot: AvailabilitySlot
    ) {
        viewModelScope.launch {
            wRepository.scheduleWorkout(
                workout,
                traineeId,
                slot
            )
        }
        loadAll()
    }
}