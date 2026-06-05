package com.example.personaltrainerapp.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltrainerapp.data.trainees.TraineeRepository
import com.example.personaltrainerapp.data.user.UserProfile
import com.example.personaltrainerapp.data.workouts.RealWorkoutsRepository
import com.example.personaltrainerapp.data.workouts.Workout
import com.example.personaltrainerapp.data.workouts.WorkoutsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class WeeklyCompletion(val label: String, val completed: Int, val total: Int)

data class TraineeStats(
    val name: String,
    val completedWorkouts: Int,
    val totalWorkouts: Int
) {
    val completionRate: Float
        get() = if (totalWorkouts == 0) 0f else completedWorkouts.toFloat() / totalWorkouts
}

data class StatsUiState(
    val isLoading: Boolean = true,
    val allWeeklyCompletions: List<WeeklyCompletion> = emptyList(),
    val perTraineeWeekly: Map<String, List<WeeklyCompletion>> = emptyMap(),
    val traineeOptions: List<Pair<String, String>> = emptyList(),
    val traineeStats: List<TraineeStats> = emptyList(),
    val totalWorkouts: Int = 0,
    val completedWorkouts: Int = 0,
    val weekOffset: Int = 0,
    val windowLabel: String = ""
) {
    val overallCompletionRate: Float
        get() = if (totalWorkouts == 0) 0f else completedWorkouts.toFloat() / totalWorkouts
}

/**
 * Manages stats state.
 */
class StatsViewModel(
    private val workoutsRepo: WorkoutsRepository = RealWorkoutsRepository(),
    private val traineeRepo: TraineeRepository = TraineeRepository()
) : ViewModel() {

    // Stats state
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState

    // Cached after the first Firestore load so week navigation never re-queries
    private var cachedPastWorkouts: List<Workout> = emptyList()
    private var cachedTrainees: List<UserProfile> = emptyList()

    // Loads workouts and trainees from Firestore and computes the initial stats window
    fun load(trainerId: String) {
        viewModelScope.launch {
            val nowMs = System.currentTimeMillis()
            cachedPastWorkouts = workoutsRepo.getTrainersWorkouts(trainerId)
                .filter { it.traineeId.isNotEmpty() && it.startTime.toDate().time < nowMs }
            cachedTrainees = traineeRepo.getAllTrainees()
                .filter { it.trainerId == trainerId }
            computeStats(0)
        }
    }

    // Shifts the displayed 8-week window by the given number of weeks
    fun shiftWeeks(delta: Int) {
        viewModelScope.launch {
            val newOffset = (_uiState.value.weekOffset + delta).coerceAtLeast(0)
            if (newOffset != _uiState.value.weekOffset) computeStats(newOffset)
        }
    }

    // Recomputes all chart and summary data from the cached workouts for the given week offset
    private fun computeStats(weekOffset: Int) {
        val today = LocalDate.now()
        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
        val shortFmt = DateTimeFormatter.ofPattern("d MMM yy")

        // Groups the given workouts into 8 weekly buckets for the current window
        fun weeklyBuckets(workouts: List<Workout>): List<WeeklyCompletion> =
            (7 downTo 0).map { weeksBack ->
                val start = monday.minusWeeks((weeksBack + weekOffset).toLong())
                val end = start.plusDays(6)
                val week = workouts.filter { w ->
                    val d = w.startTime.toDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate()
                    !d.isBefore(start) && !d.isAfter(end)
                }
                WeeklyCompletion(
                    label = start.format(shortFmt),
                    completed = week.count { w ->
                        w.exercises.isNotEmpty() && w.exercises.all { it.completed }
                    },
                    total = week.size
                )
            }

        // Aggregate weekly completions across all trainees
        val allWeeklyCompletions = weeklyBuckets(cachedPastWorkouts)

        // Per-trainee weekly completions for individual line selection
        val perTraineeWeekly = cachedTrainees.associate { t ->
            t.uid to weeklyBuckets(cachedPastWorkouts.filter { it.traineeId == t.uid })
        }

        // Date-range label shown between the Prev / Next buttons
        val windowStart = monday.minusWeeks((7 + weekOffset).toLong())
        val windowEnd = monday.minusWeeks(weekOffset.toLong()).plusDays(6)
        val windowLabel = "${windowStart.format(shortFmt)} – ${windowEnd.format(shortFmt)}"

        // Trainee uid-to-name pairs used to populate the filter chips
        val traineeOptions = cachedTrainees.map { it.uid to it.displayName.ifBlank { "Unknown" } }

        // Overall completion rate per trainee (sorted best to worst)
        val traineeStats = cachedTrainees.map { t ->
            val tw = cachedPastWorkouts.filter { it.traineeId == t.uid }
            TraineeStats(
                name = t.displayName.ifBlank { "Unknown" },
                completedWorkouts = tw.count { w ->
                    w.exercises.isNotEmpty() && w.exercises.all { it.completed }
                },
                totalWorkouts = tw.size
            )
        }.sortedByDescending { it.completionRate }

        // Total completed workouts across all time
        val totalCompleted = cachedPastWorkouts.count { w ->
            w.exercises.isNotEmpty() && w.exercises.all { it.completed }
        }

        // Updates UI state
        _uiState.value = StatsUiState(
            isLoading = false,
            allWeeklyCompletions = allWeeklyCompletions,
            perTraineeWeekly = perTraineeWeekly,
            traineeOptions = traineeOptions,
            traineeStats = traineeStats,
            totalWorkouts = cachedPastWorkouts.size,
            completedWorkouts = totalCompleted,
            weekOffset = weekOffset,
            windowLabel = windowLabel
        )
    }
}
