package com.example.personaltrainerapp.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.personaltrainerapp.data.exercises.MuscleGroups
import com.example.personaltrainerapp.data.workouts.Exercise
import com.example.personaltrainerapp.data.workouts.Workout
import com.example.personaltrainerapp.ui.common.AppHeader
import kotlin.collections.distinct
import kotlin.math.roundToInt


@Composable
fun LibraryScreen(
    trainerId: String
) {
    val startDestination = 0
    val viewModel = viewModel<LibraryViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()


    LaunchedEffect(uiState.workouts, uiState.exercises) {
        viewModel.loadAll(trainerId)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppHeader(title = "Library")

        NavHost(navController, "Tabs") {
            composable("Tabs") {
                LibraryTabs(
                    isLoading = uiState.isLoading,
                    startDestination = startDestination,
                    workouts = uiState.workouts,
                    exercises = uiState.exercises,
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable("New Exercise") {
                NewExercise(
                    navController = navController,
                    viewModel = viewModel
                )
            }
            composable("New Workout") {
                NewWorkout(
                    navController = navController,
                    viewModel = viewModel,
                    uiState = uiState,
                    trainerId = trainerId
                )
            }
            composable("Display Exercise") {

                DisplayExercise(uiState.selectedExercise, uiState)
            }
            composable("Display Workout") {

                DisplayWorkout(
                    uiState.selectedWorkout,
                    uiState,
                    navController = navController
                )
            }
        }

    }
}

@Composable
fun LibraryTabs(
    isLoading: Boolean,
    startDestination: Int,
    workouts: List<Workout>,
    exercises: List<Exercise>,
    navController: NavController,
    viewModel: LibraryViewModel
) {
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp)
    ) {
        PrimaryTabRow(selectedTabIndex = selectedDestination) {
            Tab(
                selected = selectedDestination == 0,
                onClick = {
                    selectedDestination = 0
                },
                text = {
                    Text(
                        text = "Workouts",
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
            Tab(
                selected = selectedDestination == 1,
                onClick = {
                    selectedDestination = 1
                },
                text = {
                    Text(
                        text = "Exercises",
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
        when (selectedDestination) {
            0 -> {
                WorkoutsLibrary(
                    isLoading = isLoading,
                    workouts = workouts,
                    viewModel = viewModel,
                    navController = navController
                )
            }

            1 -> {
                ExercisesLibrary(
                    isLoading = isLoading,
                    exercises = exercises,
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun WorkoutsLibrary(
    isLoading: Boolean,
    workouts: List<Workout>,
    viewModel: LibraryViewModel,
    navController: NavController
) {

    when {
        isLoading -> {
            Text("Loading Workouts...")
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                ListItem(
                    headlineContent = { Text("New Workout") },
                    modifier = Modifier.clickable(onClick = {
                        navController.navigate("New Workout")
                    }),
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Workout"
                        )
                    }
                )
                HorizontalDivider(thickness = 2.dp)
                Spacer(modifier = Modifier.size(12.dp))
                WorkoutList(
                    workouts = workouts,
                    onClick = { workout ->
                        viewModel.selectWorkout(workout)
                        navController.navigate("Display Workout")
                    })
            }
        }
    }
}


@Composable
fun NewWorkout(
    navController: NavController,
    viewModel: LibraryViewModel,
    uiState: LibraryUiState,
    trainerId: String
) {
    val exercises = remember { mutableStateListOf<Exercise>() }
    var addingExercise by remember { mutableStateOf(false) }

    val nameState = rememberTextFieldState()
    val descState = rememberTextFieldState()

    when (addingExercise) {
        true -> {
            Column() {
                AppHeader(
                    title = "Add Exercise",
                    onBack = { addingExercise = false }
                )
                LazyColumn() {
                    items(uiState.exercises) {
                        ListItem(
                            headlineContent = { Text(it.name) },
                            supportingContent = { Text(it.muscleGroup) },
                            modifier = Modifier.clickable(onClick = {
                                exercises += it
                                addingExercise = false
                            })
                        )
                    }
                }
            }
        }


        false -> {
            Column() {
                AppHeader(
                    title = "New Workout",
                    onBack = { navController.popBackStack() },
                    trailingContent = {
                        IconButton(onClick = {
                            if (!exercises.isEmpty()) {
                                val newWorkout = Workout(
                                    id = "",
                                    trainerId = trainerId,
                                    name = nameState.text as String,
                                    description = descState.text as String,
                                    exercises = exercises,
                                    estimatedMinutes = exercises.sumOf { it.estimatedMinutes },
                                    exerciseTypeSplit = exercises.map { it.muscleGroup }.distinct()
                                        .map { group ->
                                            Pair(
                                                group,
                                                ((exercises.sumOf { if (group == it.muscleGroup) it.estimatedMinutes else 0 } / exercises.sumOf { it.estimatedMinutes }
                                                    .toFloat()) * 100).roundToInt()
                                            )
                                        }
                                )
                                viewModel.saveWorkout(newWorkout)
                                navController.popBackStack()
                            }

                        }) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save Exercise"
                            )
                        }

                    }
                )
                TextField(
                    state = nameState,
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    state = descState,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                ListItem(
                    headlineContent = { Text("Add Exercise") },
                    modifier = Modifier.clickable(onClick = { addingExercise = true }),
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Exercise"
                        )

                    }
                )
                HorizontalDivider(thickness = 2.dp)
                LazyColumn {
                    items(exercises) {
                        var reps by remember { mutableStateOf(it.reps.toString()) }
                        ListItem(
                            headlineContent = { Text(it.name) },
                            supportingContent = { Text(it.muscleGroup) },
                            trailingContent = {
                                RepsInput(
                                    value = reps,
                                    onRepsChange = { r ->
                                        reps = r
                                    }
                                )

                            },
                            leadingContent = {
                                IconButton(
                                    onClick = {
                                        exercises -= it
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Remove Exercise"
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun NewExercise(
    navController: NavController,
    viewModel: LibraryViewModel
) {

    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var time by remember { mutableIntStateOf(0) }
    var tag by remember { mutableStateOf("") }
    Column() {
        AppHeader(
            title = "New Exercise",
            onBack = { navController.popBackStack() },
            trailingContent = {
                IconButton(onClick = {
                    val newExercise = Exercise(
                        name = name,
                        description = desc,
                        estimatedMinutes = time,
                        muscleGroup = tag
                    )
                    viewModel.saveExercise(newExercise)
                    navController.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save Exercise"
                    )
                }

            }
        )
        TextField(
            state = TextFieldState(""),
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            state = TextFieldState(""),
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            state = TextFieldState(""),
            label = { Text("Minutes per Rep") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )
        LazyRow() {
            items(MuscleGroups.all) { type ->
                FilterChip(
                    selected = tag == type,
                    label = { Text(type) },
                    onClick = {
                        tag = type
                    }
                )
            }
        }
    }
}

@Composable
fun ExercisesLibrary(
    isLoading: Boolean,
    exercises: List<Exercise>,
    viewModel: LibraryViewModel,
    navController: NavController
) {

    when {
        isLoading -> {
            Text("Loading Exercises...")
        }

        else -> {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                ListItem(
                    headlineContent = { Text("New Exercise") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Exercise"
                        )
                    },
                    modifier = Modifier.clickable(
                        onClick = {
                            navController.navigate("New Exercise")
                        }
                    )
                )
                HorizontalDivider(thickness = 2.dp)
                Spacer(modifier = Modifier.size(12.dp))
                ExerciseList(
                    exercises = exercises,
                    onClick = { (exercise) ->
                        //viewModel.selectExercise(exercise)
                        navController.navigate("Display Exercise")
                    }
                )
            }
        }
    }
}

@Composable
fun ExerciseList(
    exercises: List<Exercise>,
    onClick: (Exercise) -> Unit
) {
    LazyColumn() {
        items(exercises) { exercise ->
            ExerciseListItem(
                exercise = exercise,
                onClick = { onClick(exercise) }
            )
        }
    }
}


@Composable
fun WorkoutList(
    workouts: List<Workout>,
    onClick: (Workout) -> Unit
) {
    LazyColumn() {
        items(workouts) { workout ->
            WorkoutListItem(
                workout = workout,
                onClick = { onClick(workout) }
            )
        }
    }
}

@Composable
private fun WorkoutListItem(
    workout: Workout,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = workout.name,
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
                workout.estimatedMinutes.toString() + " mins: " + workout.exerciseTypeSplit.map { (group, time) -> ", $group: $time Mins" }
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun ExerciseListItem(
    exercise: Exercise,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = exercise.name,
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
                exercise.muscleGroup
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}


@Composable
fun DisplayWorkout(
    workout: Workout?,
    uiState: LibraryUiState,
    navController: NavController
) {

    Column(
        Modifier
            .fillMaxSize()
    ) {
        AppHeader(title = workout!!.name, onBack = {
            navController.popBackStack()
        })
        Spacer(modifier = Modifier.height(16.dp))
        Text(workout.description)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(workout.exercises) { exercise ->
                ListItem(
                    headlineContent = { Text(exercise.name) },
                    supportingContent = { Text(exercise.muscleGroup) },
                    trailingContent = {
                        Column {
                            Text("Reps: " + exercise.reps)

                        }
                    }
                )
            }
        }

    }
}

@Composable
fun RepsInput(
    value: String,
    onRepsChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            if (it.isEmpty() || it.all { c -> c.isDigit() }) {
                onRepsChange(it)
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        modifier = Modifier.size(80.dp, 60.dp),
        label = { Text("Reps:") }
    )
}

@Composable
fun DisplayExercise(
    exercise: Exercise?,
    uiState: LibraryUiState

) {

}

