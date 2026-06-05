package com.example.personaltrainerapp.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personaltrainerapp.ui.common.AppHeader
import kotlin.math.roundToInt

// Distinct colours assigned to individual trainee lines
private val chartLineColors = listOf(
    Color(0xFF6750A4),
    Color(0xFF00897B),
    Color(0xFFE65100),
    Color(0xFF1976D2),
    Color(0xFF7B1FA2),
    Color(0xFF558B2F),
    Color(0xFFF57F17),
    Color(0xFFC62828),
)

data class ChartSeries(val label: String, val data: List<WeeklyCompletion>, val color: Color)

/**
 * Stats screen, displays stats about a trainer's trainees.
 */
@Composable
fun StatsScreen(trainerId: String) {
    // Stats view model
    val vm = viewModel<StatsViewModel>()
    val state by vm.uiState.collectAsState()

    // Load data when the screen first enters composition
    LaunchedEffect(trainerId) { vm.load(trainerId) }

    // Empty set = "All Trainees" aggregate view
    // Non-empty set = one line per selected trainee
    var selectedTraineeIds by remember { mutableStateOf(emptySet<String>()) }

    // Stable colour per trainee based on position in traineeOptions
    val traineeColorMap = remember(state.traineeOptions) {
        state.traineeOptions.mapIndexed { i, (uid, _) ->
            uid to chartLineColors[i % chartLineColors.size]
        }.toMap()
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    // Build the list of series to pass to the chart
    val chartSeries = if (selectedTraineeIds.isEmpty()) {
        listOf(ChartSeries("All Trainees", state.allWeeklyCompletions, primaryColor))
    } else {
        state.traineeOptions
            .filter { (uid, _) -> uid in selectedTraineeIds }
            .mapNotNull { (uid, name) ->
                val data = state.perTraineeWeekly[uid] ?: return@mapNotNull null
                ChartSeries(name, data, traineeColorMap[uid] ?: primaryColor)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // App header
        AppHeader(title = "Statistics")

        Spacer(Modifier.height(16.dp))

        // Page title
        Text(
            "Training Insights",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        // Display a spinner while the initial Firestore fetch is in progress
        if (state.isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        // Overview card
        StatsCard {
            Text(
                "Overview",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SummaryItem("Total Sessions", "${state.totalWorkouts}")
                SummaryItem("Completed", "${state.completedWorkouts}")
                SummaryItem("Rate", "${(state.overallCompletionRate * 100).roundToInt()}%")
            }
        }

        Spacer(Modifier.height(12.dp))

        // Completed workouts per week card
        StatsCard {
            Text(
                "Completed Workouts Per Week",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )

            // Trainee filter chips
            if (state.traineeOptions.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedTraineeIds.isEmpty(),
                            onClick = { selectedTraineeIds = emptySet() },
                            label = { Text("All") }
                        )
                    }
                    items(state.traineeOptions) { (uid, name) ->
                        FilterChip(
                            selected = uid in selectedTraineeIds,
                            onClick = {
                                selectedTraineeIds = if (uid in selectedTraineeIds) {
                                    selectedTraineeIds - uid
                                } else {
                                    selectedTraineeIds + uid
                                }
                            },
                            label = { Text(name) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Week navigation row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { vm.shiftWeeks(8) }) { Text("< Prev") }
                Text(
                    state.windowLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { vm.shiftWeeks(-8) },
                    enabled = state.weekOffset > 0
                ) { Text("Next >") }
            }

            Spacer(Modifier.height(8.dp))

            // Display line chart
            val noData = chartSeries.all { s -> s.data.all { it.completed == 0 } }
            if (noData) {
                Text(
                    "No completed workouts in this period.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                WeeklyLineChart(
                    series = chartSeries,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                // Colour legend (only shown when individual trainees are selected)
                if (chartSeries.size > 1) {
                    Spacer(Modifier.height(12.dp))
                    chartSeries.forEach { s ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(s.color, CircleShape)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(s.label, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Completion rate by trainee card
        StatsCard {
            Text(
                "Completion Rate by Trainee",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))
            // Display each trainee's completion rate bar
            if (state.traineeStats.isEmpty()) {
                Text(
                    "No trainees assigned yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                state.traineeStats.forEachIndexed { index, stats ->
                    if (index > 0) Spacer(Modifier.height(12.dp))
                    TraineeProgressRow(stats)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

/**
 * Overview metric, e.g. Total Sessions.
 */
@Composable
private fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Metric value
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        // Metric description
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Trainee completion rate bar.
 */
@Composable
private fun TraineeProgressRow(stats: TraineeStats) {
    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Trainee name
            Text(
                stats.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            // Completed / total and percentage
            Text(
                "${stats.completedWorkouts}/${stats.totalWorkouts} (${(stats.completionRate * 100).roundToInt()}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(4.dp))

        // Completion rate progress bar
        LinearProgressIndicator(
            progress = { stats.completionRate },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Line chart showing number of completed workouts per week.
 */
@Composable
private fun WeeklyLineChart(series: List<ChartSeries>, modifier: Modifier = Modifier) {
    // TextMeasurer for measuring a text so that it's ready to be drawn
    val textMeasurer = rememberTextMeasurer()

    // Theme colours
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val surface = MaterialTheme.colorScheme.surface
    val isSingle = series.size == 1

    Canvas(modifier = modifier) {
        // Chart padding
        val leftPad = 32.dp.toPx()
        val bottomPad = 32.dp.toPx()
        val rightPad = 4.dp.toPx()
        val topPad = 8.dp.toPx()

        // Chart width, height and origin
        val chartW = size.width - leftPad - rightPad
        val chartH = size.height - topPad - bottomPad
        val originX = leftPad
        val originY = topPad + chartH

        // Use the max completed workouts count across all series for a consistent Y scale
        val maxVal = series.maxOf { s -> s.data.maxOf { it.completed } }.coerceAtLeast(1)

        // Evenly space data points across the chart width
        val dataSize = series.first().data.size
        val xPositions = if (dataSize <= 1) {
            List(dataSize) { originX + chartW / 2f }
        } else {
            (0 until dataSize).map { i -> originX + i * (chartW / (dataSize - 1).toFloat()) }
        }

        // Horizontal grid lines and Y-axis labels (0, mid, max)
        val yLevels = buildList {
            add(0)
            if (maxVal >= 2) add(maxVal / 2)
            add(maxVal)
        }
        yLevels.forEach { v ->
            val y = originY - (v.toFloat() / maxVal) * chartH
            drawLine(surfaceVariant, Offset(originX, y), Offset(originX + chartW, y), 1.dp.toPx())
            val label = textMeasurer.measure("$v", TextStyle(fontSize = 9.sp, color = onSurfaceVariant))
            drawText(
                label,
                topLeft = Offset(originX - label.size.width - 3.dp.toPx(), y - label.size.height / 2f)
            )
        }

        // Y axis and X axis
        drawLine(onSurface, Offset(originX, topPad), Offset(originX, originY), 1.5f.dp.toPx())
        drawLine(onSurface, Offset(originX, originY), Offset(originX + chartW, originY), 1.5f.dp.toPx())

        // X-axis week labels (every other week to avoid crowding)
        series.first().data.forEachIndexed { i, item ->
            if (i % 2 == 0) {
                val label = textMeasurer.measure(
                    item.label,
                    TextStyle(fontSize = 8.sp, color = onSurfaceVariant)
                )
                drawText(
                    label,
                    topLeft = Offset(xPositions[i] - label.size.width / 2f, originY + 4.dp.toPx())
                )
            }
        }

        // Draw each series
        series.forEach { chartSeries ->
            // Pixel coordinates of each data point
            val points = chartSeries.data.mapIndexed { i, item ->
                Offset(xPositions[i], originY - (item.completed.toFloat() / maxVal) * chartH)
            }

            // Translucent fill under the line (only drawn for a single serries)
            if (isSingle && points.size >= 2) {
                val fillPath = Path().apply {
                    moveTo(points.first().x, originY)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, originY)
                    close()
                }
                drawPath(fillPath, chartSeries.color, alpha = 0.12f)
            }

            // Line segments connecting consecutive data points
            for (i in 0 until points.size - 1) {
                drawLine(
                    chartSeries.color,
                    points[i],
                    points[i + 1],
                    2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Dot markers
            points.forEach { pt ->
                drawCircle(chartSeries.color, 4.dp.toPx(), pt)
                drawCircle(surface, 2.dp.toPx(), pt)
            }
        }
    }
}

/**
 * Card used by each section of the stats screen.
 */
@Composable
private fun StatsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}
