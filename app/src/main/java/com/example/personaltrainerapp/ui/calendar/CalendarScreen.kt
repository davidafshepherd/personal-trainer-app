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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.personaltrainerapp.ui.common.AppHeader
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

enum class Availability {
    IDEAL,
    MAYBE,
    BUSY
}

@Composable
fun CalendarScreen() {
    val today = LocalDate.now()
    val firstWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val maxDate = today.plusMonths(6)

    var currentWeekStart by remember { mutableStateOf(firstWeekStart) }

    var availability by remember {
        mutableStateOf<Map<String, Availability>>(emptyMap())
    }

    val weekDates = (0..6).map { currentWeekStart.plusDays(it.toLong()) }
    val weekEnd = weekDates.last()

    val canGoBack = currentWeekStart.isAfter(firstWeekStart)
    val canGoForward = weekEnd.isBefore(maxDate)

    // Wider availability window; grid scrolls vertically.
    val hours = (6..21)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppHeader(title = "Calendar")

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
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

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(hours.count()) { index ->
                val hour = hours.first + index

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "%02d:00".format(hour),
                        modifier = Modifier.width(52.dp)
                    )

                    weekDates.forEach { date ->
                        val key = availabilityKey(date, hour)
                        val currentAvailability = availability[key] ?: Availability.IDEAL

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(3.dp)
                                .aspectRatio(1f)
                                .background(
                                    color = colorFor(currentAvailability),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    availability = availability.toMutableMap().also { updated ->
                                        updated[key] = nextAvailability(currentAvailability)
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
            LegendItem("Ideal", colorFor(Availability.IDEAL))
            LegendItem("Maybe", colorFor(Availability.MAYBE))
            LegendItem("Busy", colorFor(Availability.BUSY))
        }
    }
}

private fun availabilityKey(date: LocalDate, hour: Int): String {
    return "${date}_${hour}"
}

private fun nextAvailability(current: Availability): Availability {
    return when (current) {
        Availability.IDEAL -> Availability.MAYBE
        Availability.MAYBE -> Availability.BUSY
        Availability.BUSY -> Availability.IDEAL
    }
}

private fun colorFor(availability: Availability): Color {
    return when (availability) {
        Availability.IDEAL -> Color(0xFFA5D6A7)
        Availability.MAYBE -> Color(0xFFFFF59D)
        Availability.BUSY -> Color(0xFFEF9A9A)
    }
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