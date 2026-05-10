package com.example.personaltrainerapp.ui.trainees

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.personaltrainerapp.data.trainer.TrainerRequest
import com.example.personaltrainerapp.data.user.UserProfile
import com.example.personaltrainerapp.data.workouts.Workout
import com.example.personaltrainerapp.ui.calendar.CalendarScreen
import com.example.personaltrainerapp.ui.common.AppHeader
import com.example.personaltrainerapp.ui.library.ExerciseList
import com.example.personaltrainerapp.ui.library.LibraryViewModel
import com.example.personaltrainerapp.ui.library.WorkoutList
import com.example.personaltrainerapp.ui.trainer.TrainerViewModel
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.roundToInt

@Composable
fun TraineesScreen(
    trainerId: String = "demo-user"
) {

    val navController = rememberNavController()

    val traineeViewModel = remember { TraineeViewModel() }
    val traineeUiState by traineeViewModel.uiState.collectAsState()

    val libraryViewModel = remember { LibraryViewModel() }
    val libraryUiState by libraryViewModel.uiState.collectAsState()

    LaunchedEffect(traineeUiState.myTrainees, traineeUiState.allTrainees) {
        traineeViewModel.loadTrainees(trainerId)
    }

    NavHost(navController, startDestination = "Trainees") {
        composable("Trainees") {
            traineeViewModel.loadAvailability(trainerId)
            MyTraineesScreen(
                isLoading = traineeUiState.isLoading,
                myTrainees = traineeUiState.myTrainees,
                viewTrainee = { trainee ->
                    traineeViewModel.selectTrainee(trainee.uid)
                    navController.navigate("selectedTrainee")
                },
                trainerId = trainerId,
                navController = navController,
                viewModel = traineeViewModel,
                availability = traineeUiState.availability
            )
        }
        composable("selectedTrainee") {
            traineeUiState.selectedTrainee?.let { trainee ->
                DisplayTrainee(
                    trainee = trainee,
                    onBack = { navController.popBackStack() },
                    navController = navController,
                    traineeViewModel = traineeViewModel,
                    libraryViewModel = libraryViewModel,
                    workouts = libraryUiState.workouts.filter { it.traineeId == trainee.uid }
                )
            }
        }

        composable("Schedule Workout") {
            LaunchedEffect(libraryUiState.workouts) {
                libraryViewModel.loadAll(trainerId)
            }
            Column() {
                AppHeader(
                    title = "Schedule Workout",
                    onBack = { navController.popBackStack() }
                )
                WorkoutList(
                    workouts = libraryUiState.workouts,
                    onClick = { workout ->
                        val traineeId = traineeUiState.selectedTrainee?.uid
                        val slot = traineeUiState.tempSlot
                        if (traineeId != null && slot != null) {
                            libraryViewModel.scheduleWorkout(
                                workout = workout,
                                traineeId = traineeId,
                                trainerId = trainerId,
                                slot = slot
                            )
                            navController.popBackStack()
                        }
                    }
                )
            }
        }
        composable("Workout Dashboard") {
            val workout = libraryUiState.selectedWorkout
            val trainee = traineeUiState.selectedTrainee
            if (workout != null && trainee != null) {
                WorkoutDashboard(
                    workout = workout,
                    trainee = trainee,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        composable("Requests") {
            LaunchedEffect(traineeUiState.myRequests) {
                traineeViewModel.loadRequests(trainerId)
                traineeViewModel.loadAvailability(trainerId)
            }
            DisplayRequests(
                isLoading = traineeUiState.isLoading,
                requests = traineeUiState.myRequests.filter { it.status == "PENDING" },
                onBack = { navController.popBackStack() },
                trainees = traineeUiState.allTrainees,
                canAccept = traineeUiState.availability > traineeUiState.myTrainees.size || traineeUiState.availability == -1,
                viewModel = traineeViewModel
            )
        }
    }
}

@Composable
fun DisplayRequests(
    isLoading: Boolean,
    requests: List<TrainerRequest>,
    onBack: () -> Unit,
    trainees: List<UserProfile>,
    canAccept: Boolean,
    viewModel: TraineeViewModel
) {
    Log.d("RequestsTab", "checka")
    Column() {
        AppHeader(
            title = "Trainer Requests",
            onBack = onBack
        )
        when{
            isLoading ->{
                Log.d("RequestsTab", "checklo")
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { Text("Loading Requests...") }
            }

            else -> {
                Log.d("RequestsTab", "checkr")
                LazyColumn() {
                    items(requests) { request ->
                        val trainee = trainees.first { it.uid == request.traineeId }
                        Log.d("RequestsTab", "check1")

                        ListItem(
                            headlineContent = { Text(trainee.displayName) },
                            supportingContent = { Text("" + trainee.tags) },
                            trailingContent = {
                                Row {
                                    IconButton(
                                        onClick = {
                                            if (canAccept) {
                                                viewModel.assignTrainee(
                                                    trainerId = request.trainerId,
                                                    traineeId = request.traineeId
                                                )
                                                viewModel.updateRequestStatus(request.id, "ACCEPTED")
                                                viewModel.loadRequests(trainerId = request.trainerId)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircleOutline,
                                            contentDescription = "Accept"
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.updateRequestStatus(request.id, "REJECTED")
                                            viewModel.loadRequests(trainerId = request.trainerId)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Block,
                                            contentDescription = "Reject"
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
        Log.d("RequestsTab", requests.size.toString())


    }
}

@Composable
fun WorkoutDashboard(
    workout: Workout,
    trainee: UserProfile,
    onBack: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppHeader(
            title = trainee.displayName + ": " + workout.name,
            onBack = onBack
        )
        Log.d("TraineeScreen", "header")
        val progress = if (workout.exercises.isNotEmpty()) {
            workout.exercises.count { it.completed }.toFloat() / workout.exercises.size.toFloat()
        } else 0f
        Log.d("TraineeScreen", "progress: $progress")
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(200.dp, 200.dp),
                strokeWidth = 5.dp
            )
            val progressText = if (progress.isNaN()) "0%" else (progress * 100).roundToInt().toString()+"%"
            Text(text = progressText,
                fontSize = 40.sp,
             fontWeight = FontWeight.Bold)
        }

        Log.d("TraineeScreen", "circle")
        LazyColumn {
            items(workout.exercises) { it ->
                ListItem(
                    modifier = Modifier.alpha(if (it.completed) 0.5F else 1F),
                    headlineContent = { Text(it.name) },
                    supportingContent = { Text(it.muscleGroups.toString()) },
                    trailingContent = {
                        Column {
                            Text("Reps: " + it.reps)
                        }
                    }
                )
                LinearProgressIndicator(progress = {
                    (if (it.completed) {
                        1
                    } else {
                        0
                    }).toFloat()
                }, modifier = Modifier.fillMaxSize())
            }
        }
        Log.d("TraineeScreen", "column")
    }
}

@Composable
private fun DisplayTrainee(
    trainee: UserProfile,
    onBack: () -> Unit,
    navController: NavController,
    traineeViewModel: TraineeViewModel,
    libraryViewModel: LibraryViewModel,
    workouts: List<Workout>
) {
    Log.d("TraineeScreen", "Working")
    var canSeeCurrentWorkout by remember { mutableStateOf(false) }
    
    LaunchedEffect(workouts) {
        try {
            val currentWorkout = workouts.firstOrNull { workout ->

                workout.startTime.toInstant().isBefore(Timestamp.now().toInstant()) && workout.endTime.toInstant().isAfter(
                    Timestamp.now().toInstant()
                )
            }
            if (currentWorkout != null) {
                libraryViewModel.selectWorkout(currentWorkout)
                canSeeCurrentWorkout = true
            } else {
                canSeeCurrentWorkout = false
            }
        } catch (e: Exception) {
            canSeeCurrentWorkout = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppHeader(
            title = trainee.displayName,
            onBack = onBack
        )

        LazyRow() {
            items(trainee.tags) { tag ->
                FilterChip(
                    selected = true,
                    label = { Text(tag) },
                    onClick = {}
                )
            }
        }

        Button(
            onClick = {
                navController.navigate("Workout Dashboard")
            },
            enabled = canSeeCurrentWorkout
        ) {
            Text("Current Workout")
        }
        CalendarScreen(
            traineeId = trainee.uid,
            forDisplay = true,
            onClick = { traineeId: String, date: LocalDate, hour: Int ->
                traineeViewModel.setTempSlot(traineeId, date, hour)
                navController.navigate("Schedule Workout")
            }
        )
    }
}


@Composable
private fun MyTraineesScreen(
    isLoading: Boolean,
    myTrainees: List<UserProfile>,
    viewTrainee: (UserProfile) -> Unit,
    trainerId: String,
    viewModel: TrainerViewModel,
    navController: NavController
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var input by remember { mutableIntStateOf(0) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppHeader(
            title = "Trainees",
            trailingContent = {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Set Availability") },
                            onClick = {
                                menuExpanded = false
                                showDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Approve/Deny Request") },
                            onClick = {
                                menuExpanded = false
                                navController.navigate("requests")
                            }
                        )
                    }
                }
            }
        )
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Set Availability for Trainees") },

                text = {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {
                            if (it.all { c -> c.isDigit() }) {
                                input = it.toInt()
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        placeholder = { Text("e.g. 5") }
                    )
                },

                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.setTrainerAvailability(trainerId, availability = input)
                            showDialog = false
                        }
                    ) {
                        Text("Set")
                    }
                },

                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        when {
            isLoading -> {
                Text("Loading Trainees...")
            }

            myTrainees.isEmpty() -> {
                Text("You have no trainees")
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(myTrainees) { trainee ->
                        TraineeListItem(
                            trainee = trainee,
                            onClick = { viewTrainee(trainee) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TraineeListItem(
    trainee: UserProfile,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.padding(6.dp)
            )
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column {
            Text(
                text = trainee.displayName,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = trainee.tags.joinToString(", ") { tag ->
                    tag.replaceFirstChar(Char::uppercase)
                }
            )
        }
    }
}