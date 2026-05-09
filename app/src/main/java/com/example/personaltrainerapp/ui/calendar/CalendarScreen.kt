package com.example.personaltrainerapp.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personaltrainerapp.data.calendar.AvailabilityStatus
import com.example.personaltrainerapp.data.workouts.Workout
import com.example.personaltrainerapp.ui.common.AppHeader
import com.example.personaltrainerapp.ui.workouts.WorkoutsViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun CalendarScreen(
    traineeId: String = "demo-trainee",
    forDisplay: Boolean = false,
    onClick: (String, LocalDate, Int) -> Unit = { _, _, _ -> }
) {
    val viewModel = viewModel<CalendarViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    val workoutsViewModel = viewModel<WorkoutsViewModel>()
    val workoutsUiState by workoutsViewModel.uiState.collectAsState()

    val today = LocalDate.now()
    val firstWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val maxDate = today.plusMonths(6)

    var currentWeekStart by remember { mutableStateOf(firstWeekStart) }

    val weekDates = (0..6).map { currentWeekStart.plusDays(it.toLong()) }
    val weekEnd = weekDates.last()

    val canGoBack = currentWeekStart.isAfter(firstWeekStart)
    val canGoForward = weekEnd.isBefore(maxDate)

    val hours = (6..21)

    LaunchedEffect(traineeId, currentWeekStart) {
        viewModel.loadAvailability(
            traineeId = traineeId,
            startDate = weekDates.first(),
            endDate = weekDates.last()
        )

        workoutsViewModel.loadWorkouts(traineeId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        if (!forDisplay) {
            AppHeader(title = "Calendar")
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    if (canGoBack) {
                        currentWeekStart = currentWeekStart.minusWeeks(1)
                    }
                },
                enabled = canGoBack
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous week"
                )
            }

            Text(
                text = "${formatDate(weekDates.first())} - ${formatDate(weekDates.last())}",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    if (canGoForward) {
                        currentWeekStart = currentWeekStart.plusWeeks(1)
                    }
                },
                enabled = canGoForward
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next week"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            Spacer(modifier = Modifier.width(52.dp))

            weekDates.forEach { date ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(dayLabel(date))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.error != null) {
            Text(uiState.error!!)
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(hours.count()) { index ->
                val hour = hours.first + index

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "%02d:00".format(hour),
                        modifier = Modifier.width(52.dp)
                    )

                    weekDates.forEach { date ->
                        val key = availabilityKey(date, hour)
                        val currentAvailability =
                            uiState.slots[key] ?: AvailabilityStatus.IDEAL

                        val hasWorkout =
                            hasWorkoutAtSlot(
                                workouts = workoutsUiState.workouts,
                                date = date,
                                hour = hour
                            )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(3.dp)
                                .aspectRatio(1f)
                                .background(
                                    color = if (hasWorkout) {
                                        workoutColor()
                                    } else {
                                        colorFor(currentAvailability)
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable(enabled = !hasWorkout) {
                                    if (forDisplay) {
                                        if (viewModel.checkAvailability(date = date, hour = hour)) {
                                            onClick(traineeId, date, hour)
                                        }
                                    } else {
                                        viewModel.cycleAvailability(
                                            traineeId = traineeId,
                                            date = date,
                                            hour = hour
                                        )
                                    }
                                }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            LegendItem("Ideal", colorFor(AvailabilityStatus.IDEAL))
            LegendItem("Maybe", colorFor(AvailabilityStatus.MAYBE))
            LegendItem("Busy", colorFor(AvailabilityStatus.BUSY))
            LegendItem("Workout", workoutColor())
        }
    }
}

private fun hasWorkoutAtSlot(
    workouts: List<Workout>,
    date: LocalDate,
    hour: Int
): Boolean {
    val slotStart = date.atTime(hour, 0)
    val slotEnd = slotStart.plusHours(1)

    return workouts.any { workout ->
        val workoutStart = workout.startTime.toLocalDateTime()
        val workoutEnd = workout.endTime.toLocalDateTime()

        workoutStart.isBefore(slotEnd) && workoutEnd.isAfter(slotStart)
    }
}

private fun com.google.firebase.Timestamp.toLocalDateTime(): LocalDateTime {
    return this.toDate()
        .toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}

private fun colorFor(status: AvailabilityStatus): Color {
    return when (status) {
        AvailabilityStatus.IDEAL -> Color(0xFFA5D6A7)
        AvailabilityStatus.MAYBE -> Color(0xFFFFF59D)
        AvailabilityStatus.BUSY -> Color(0xFFEF9A9A)
    }
}

private fun workoutColor(): Color {
    return Color(0xFF90CAF9)
}

private fun formatDate(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
}

private fun dayLabel(date: LocalDate): String {
    return when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "M"
        DayOfWeek.TUESDAY -> "T"
        DayOfWeek.WEDNESDAY -> "W"
        DayOfWeek.THURSDAY -> "T"
        DayOfWeek.FRIDAY -> "F"
        DayOfWeek.SATURDAY -> "S"
        DayOfWeek.SUNDAY -> "S"
    }
}

@Composable
private fun LegendItem(
    label: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(color, shape = RoundedCornerShape(3.dp))
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(label)
    }
}