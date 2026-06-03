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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.personaltrainerapp.ui.library.LibraryUiState
import com.example.personaltrainerapp.ui.library.ExerciseList
import com.example.personaltrainerapp.ui.library.LibraryViewModel
import com.example.personaltrainerapp.ui.library.WorkoutList
import com.example.personaltrainerapp.ui.trainer.TrainerViewModel
import com.example.personaltrainerapp.ui.library.WorkoutListItem
import com.example.personaltrainerapp.ui.profile.ProfileSectionHeader
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.roundToInt
import com.example.personaltrainerapp.data.notifications.NotificationRepository
import kotlinx.coroutines.launch

import java.time.format.DateTimeFormatter

@Composable
fun TraineesScreen(trainerId: String) {

    val navController = rememberNavController()

    val traineeViewModel = remember { TraineeViewModel() }
    val traineeUiState by traineeViewModel.uiState.collectAsState()

    val libraryViewModel = remember { LibraryViewModel() }
    val libraryUiState by libraryViewModel.uiState.collectAsState()

    val trainerViewModel = remember { TrainerViewModel() }
    val trainerUiState by trainerViewModel.uiState.collectAsState()

    val notificationRepository = remember { NotificationRepository() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(traineeUiState.myTrainees, traineeUiState.allTrainees) {
        traineeViewModel.loadTrainees(trainerId)
    }
    traineeViewModel.loadAvailability(trainerId)
    Log.d("TraineeRepository", "Trainees Screen: " + traineeUiState.availability)

    NavHost(navController, startDestination = "Trainees") {
        composable("Trainees") {
            traineeViewModel.loadAvailability(trainerId)
            MyTraineesScreen(
                isLoading = traineeUiState.isLoading,
                myTrainees = traineeUiState.myTrainees,
                viewTrainee = { trainee ->
                    traineeViewModel.selectTrainee(trainee.uid)
                    navController.navigate("selectedTrainee/${trainee.uid}")
                },
                trainerId = trainerId,
                navController = navController,
                viewModel = traineeViewModel,
                uiState = traineeUiState
            )
        }
        composable("selectedTrainee/{id}") { backStackEntry ->

            val id = backStackEntry.arguments?.getString("id")!!
            DisplayTrainee(
                trainerId,
                onBack = { navController.popBackStack() },
                navController = navController,
                traineeViewModel = traineeViewModel,
                traineeUiState = traineeUiState,
                libraryViewModel = libraryViewModel,
                libraryUiState = libraryUiState,
                traineeId = id
            )
        }
        composable("ScheduleWorkout/{id}") { backStackEntry ->
            val traineeId = backStackEntry.arguments?.getString("id")!!
            Log.d("Screen", "schedule")
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
                        val slot = traineeUiState.tempSlot
                        if (traineeId != null && slot != null) {
                            libraryViewModel.scheduleWorkout(
                                workout = workout,
                                traineeId = traineeId,
                                trainerId = trainerId,
                                slot = slot
                            )

                            coroutineScope.launch {
                                notificationRepository.createWorkoutScheduledJob(
                                    traineeId = traineeId,
                                    workoutId = workout.id
                                )
                            }

                            val trainee = traineeUiState.allTrainees.firstOrNull { it.uid == traineeId }

                            if (trainee?.email?.isNotBlank() == true) {
                                coroutineScope.launch {
                                    notificationRepository.createWorkoutScheduledEmail(
                                        traineeEmail = trainee.email,
                                        workoutName = workout.name
                                    )
                                }
                            }

                            navController.popBackStack()
                        }
                    },
                    canOpen = false
                )
            }
        }
        composable("Workout Dashboard/{tId}/{wId}") { backStackEntry ->
            val wId = backStackEntry.arguments?.getString("wId")!!
            val tId = backStackEntry.arguments?.getString("tId")!!
            Log.d("Screen", "dash")

            WorkoutDashboard(
                workoutId = wId,
                traineeId = tId,
                libraryUiState = libraryUiState,
                traineeUiState = traineeUiState,
                onBack = {
                    navController.popBackStack()
                }
            )

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
        when {
            isLoading -> {
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
                                                viewModel.updateRequestStatus(
                                                    request.id,
                                                    "ACCEPTED"
                                                )
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
    workoutId: String,
    traineeId: String,
    libraryUiState: LibraryUiState,
    traineeUiState: TraineeUiState,
    onBack: () -> Unit
) {
    val trainee = traineeUiState.allTrainees.firstOrNull { it.uid == traineeId }!!
    val workout = libraryUiState.workouts.firstOrNull { it.id == workoutId }!!
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.background(MaterialTheme.colorScheme.background),

        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
            val progressText =
                if (progress.isNaN()) "0%" else (progress * 100).roundToInt().toString() + "%"
            Text(
                text = progressText,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
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
    trainerId: String,
    onBack: () -> Unit,
    navController: NavController,
    traineeViewModel: TraineeViewModel,
    traineeUiState: TraineeUiState,
    libraryViewModel: LibraryViewModel,
    libraryUiState: LibraryUiState,
    traineeId: String
) {
    val trainee = traineeUiState.allTrainees.firstOrNull { it.uid == traineeId }
    libraryViewModel.loadAll(trainerId)
    when {
        traineeUiState.isLoading || libraryUiState.isLoading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) { CircularProgressIndicator() }
        }

        else -> {

            val now = Timestamp.now()
            var currentWorkout = libraryUiState.workouts.firstOrNull { workout ->
                workout.startTime <= now &&
                        workout.endTime >= now &&
                        workout.traineeId == trainee!!.uid
            }

            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                AppHeader(
                    title = trainee!!.displayName,
                    onBack = onBack
                )

                ProfileSectionHeader("Exercise Tags")
                LazyRow() {
                    items(trainee.tags) { tag ->
                        FilterChip(
                            selected = true,
                            label = { Text(tag) },
                            onClick = {}
                        )
                    }
                }

                currentWorkout?.let {
                    ProfileSectionHeader("Current Workout")
                    WorkoutListItem(
                        workout = currentWorkout,
                        onClick = { navController.navigate("Workout Dashboard/${traineeId}/${currentWorkout.id}") }
                    )
                }

                ProfileSectionHeader("Upcoming Workouts")
                val workouts = libraryUiState.workouts.filter { it.traineeId == traineeId && currentWorkout?.id != it.id && it.startTime > now}
                LazyColumn (modifier = Modifier.height(((75 * workouts.size).coerceAtMost(115)).dp)){
                    items(workouts) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = it.name,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            supportingContent = {
                                Text(
                                    "${it.startTime.formatDateTime()} - ${it.endTime.formatDateTime(true)}"
                                )
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = { libraryViewModel.removeWorkout(it.id) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "Remove Workout from Calendar"
                                    )
                                }
                            }
                        )
                    }
                }

                ProfileSectionHeader("Calendar")
                Column(modifier = Modifier.height(1000.dp)) {
                    CalendarScreen(
                        traineeId = trainee.uid,
                        forDisplay = true,
                        onClick = { traineeId: String, date: LocalDate, hour: Int ->
                            traineeViewModel.setTempSlot(traineeId, date, hour)
                            navController.navigate("ScheduleWorkout/${traineeId}")
                        }
                    )
                }

            }
        }
    }
}
private fun Timestamp.formatDateTime(justHour: Boolean = false): String {
    var pattern =  "d/M/yy · h:mm a"
    if (justHour){
        pattern =  "h:mm a"
    }
    return this.toDate()
        .toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(DateTimeFormatter.ofPattern(pattern))
}

@Composable
private fun MyTraineesScreen(
    isLoading: Boolean,
    myTrainees: List<UserProfile>,
    viewTrainee: (UserProfile) -> Unit,
    trainerId: String,
    viewModel: TraineeViewModel,
    uiState: TraineeUiState,
    navController: NavController
) {
    var input by remember { mutableIntStateOf(uiState.availability) }
    var unlimited by remember { mutableStateOf(uiState.availability == -1) }
    LaunchedEffect(uiState.availability) {
        input = uiState.availability
        unlimited = uiState.availability == -1
    }
    LaunchedEffect(uiState.myTrainees) {
        viewModel.loadTrainees(trainerId)
    }
    Log.d("Test", "Availability: " + uiState.availability)
    var menuExpanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

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
            when (isLoading) {
                true -> Unit
                false -> AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Availability for Trainees") },
                    text = {
                        Column() {
                            OutlinedTextField(
                                value = if (input == 0 || input == -1) {
                                    ""
                                } else {
                                    input.toString()
                                },
                                enabled = !unlimited,
                                onValueChange = { value ->
                                    if (!value.isEmpty() && value.all { c -> c.isDigit() }) {
                                        input = value.toInt()
                                    }
                                    if (value.isEmpty()) {
                                        input = 0
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                singleLine = true,
                            )
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Unlimited: ")
                                Checkbox(
                                    checked = unlimited,
                                    onCheckedChange = {
                                        unlimited = it
                                    }
                                )
                            }
                        }

                    },

                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (unlimited) {
                                    input = -1
                                }
                                viewModel.setTrainerAvailability(trainerId, input)
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
        }
        when {
            isLoading -> {
                Text("Loading Trainees...")
            }

            myTrainees.isEmpty() -> {
                Text("You have no trainees.")
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
