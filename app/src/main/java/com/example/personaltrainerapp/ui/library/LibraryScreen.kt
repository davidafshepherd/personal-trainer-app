package com.example.personaltrainerapp.ui.library

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.InsertPhoto
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material3.AlertDialog
import coil3.compose.AsyncImage
import com.example.personaltrainerapp.data.exercises.MuscleGroups
import com.example.personaltrainerapp.data.workouts.Exercise
import com.example.personaltrainerapp.data.workouts.Workout
import com.example.personaltrainerapp.ui.common.AppHeader
import com.example.personaltrainerapp.ui.profile.ProfileSectionHeader
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import kotlin.collections.distinct
import kotlin.math.roundToInt
import kotlin.text.all
import kotlin.text.isEmpty


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
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 12.dp)
    ) {
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
                    navController = navController, viewModel = viewModel, trainerId = trainerId
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
                DisplayExercise(
                    uiState.selectedExercise!!, onBack = { navController.popBackStack() })
            }
            composable("Display Workout") {
                DisplayWorkout(
                    uiState.selectedWorkout, uiState, navController = navController
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
            .padding(vertical = 12.dp, horizontal = 20.dp)
    ) {
        AppHeader(title = "Library")
        SecondaryTabRow(selectedTabIndex = selectedDestination) {
            Tab(selected = selectedDestination == 0, onClick = {
                selectedDestination = 0
            }, text = {
                Text(
                    text = "Workouts", maxLines = 2, overflow = TextOverflow.Ellipsis
                )
            })
            Tab(selected = selectedDestination == 1, onClick = {
                selectedDestination = 1
            }, text = {
                Text(
                    text = "Exercises", maxLines = 2, overflow = TextOverflow.Ellipsis
                )
            })
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
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) { Text("Loading Workouts...") }
        }

        else -> {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                ListItem(
                    headlineContent = { Text("New Workout") },
                    modifier = Modifier.clickable(onClick = {
                        navController.navigate("New Workout")
                    }),
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Add, contentDescription = "New Workout"
                        )
                    })
                HorizontalDivider(thickness = 2.dp)
                Spacer(modifier = Modifier.size(12.dp))
                WorkoutList(
                    workouts = workouts, onClick = { workout ->
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

    var currentExercise by remember { mutableIntStateOf(0) }

    val nameState = rememberTextFieldState()
    val descState = rememberTextFieldState()

    var showDialog by remember { mutableStateOf(false) }

    when (addingExercise) {
        true -> {
            Column() {
                AppHeader(
                    title = "Add Exercise", onBack = { addingExercise = false })
                LazyColumn() {
                    items(uiState.exercises) {
                        ListItem(
                            headlineContent = { Text(it.name) },
                            supportingContent = { Text(it.muscleGroups.toString()) },
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
                                var groupTypeSplit = mutableMapOf<String, Int>()
                                var groups = exercises.flatMap { it.muscleGroups }.distinct()
                                groups.forEach { group ->
                                    var total = 0
                                    for (exercise: Exercise in exercises) {
                                        if (group in exercise.muscleGroups) {
                                            total += exercise.estimatedMinutes * exercise.reps
                                        }
                                    }
                                    groupTypeSplit[group] =
                                        ((total.toFloat() / exercises.sumOf { it.estimatedMinutes * it.reps }) * 100).roundToInt()
                                }

                                val newWorkout = Workout(
                                    trainerId = trainerId,
                                    name = nameState.text as String,
                                    description = descState.text as String,
                                    exercises = exercises,
                                    estimatedMinutes = exercises.sumOf { it.estimatedMinutes * it.reps },
                                    exerciseTypeSplit = groupTypeSplit,
                                )

                                viewModel.saveWorkout(newWorkout)
                                viewModel.loadAll(trainerId)
                                navController.popBackStack()
                            }

                        }) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save Exercise"
                            )
                        }

                    })
                TextField(
                    state = nameState, label = { Text("Name") }, modifier = Modifier.fillMaxWidth()
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
                            imageVector = Icons.Default.Add, contentDescription = "Add Exercise"
                        )

                    })
                HorizontalDivider(thickness = 2.dp)
                LazyColumn {
                    items(exercises) {
                        ListItem(
                            headlineContent = { Text(it.name) },
                            supportingContent = { Text(it.muscleGroups.toString()) },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        currentExercise = exercises.indexOf(it)
                                        showDialog = true
                                    }) {
                                        Icon(
                                            imageVector = if (it.uri == "") Icons.Default.AddPhotoAlternate else Icons.Default.InsertPhoto,
                                            contentDescription = "Add Media"
                                        )
                                    }
                                    OutlinedTextField(
                                        value = if (it.reps == 0) {
                                            ""
                                        } else {
                                            it.reps.toString()
                                        },
                                        onValueChange = { value ->
                                            if (value.all { c -> c.isDigit() } and !value.isEmpty()) {
                                                exercises[exercises.indexOf(it)] =
                                                    it.copy(reps = value.toInt())
                                            }
                                            if (value.isEmpty()) {
                                                exercises[exercises.indexOf(it)] = it.copy(reps = 0)
                                            }
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number
                                        ),
                                        modifier = Modifier.size(60.dp, 60.dp),
                                        label = { Text("Reps") })
                                }

                            },
                            leadingContent = {
                                IconButton(
                                    onClick = {
                                        exercises -= it
                                    }) {
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

            if (showDialog) {
                Dialog(onDismissRequest = { showDialog = false }, content = {
                    var type by remember { mutableStateOf("video") }
                    val launcher = rememberLauncherForActivityResult(
                        ActivityResultContracts.GetContent()
                    ) { uri ->
                        uri?.let {
                            exercises[currentExercise] = exercises[currentExercise].copy(
                                uri = uri.toString(),
                                mediaType = type
                            )
                        }
                        showDialog = false
                    }

                    Column {
                        Button(
                            onClick = {
                                type = "image"
                                launcher.launch("image/*")
                            }) {
                            Text("Select Image")
                        }

                        Button(
                            onClick = {
                                type = "video"
                                launcher.launch("video/*")
                            }) {
                            Text("Select Video")
                        }
                    }
                })
            }
        }
    }
}

@Composable
fun NewMediaPlayer(mediaUrl: String) {
    Log.d("Multimedia", "Check")
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(mediaUrl))
            prepare()
            playWhenReady = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
            }
        }, modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    )
}


@Composable
fun NewExercise(
    navController: NavController, viewModel: LibraryViewModel, trainerId: String
) {

    val nameState = rememberTextFieldState()
    val descState = rememberTextFieldState()
    var timeState by remember { mutableStateOf("") }
    val tags = remember { mutableStateListOf<String>() }
    Column() {
        AppHeader(
            title = "New Exercise",
            onBack = { navController.popBackStack() },
            trailingContent = {
                IconButton(onClick = {
                    val newExercise = Exercise(
                        name = nameState.text.toString(),
                        description = descState.text.toString(),
                        estimatedMinutes = timeState.toIntOrNull() ?: 0,
                        muscleGroups = tags,
                        trainerId = trainerId
                    )
                    viewModel.saveExercise(newExercise)
                    viewModel.loadAll(trainerId)
                    navController.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.Default.Save, contentDescription = "Save Exercise"
                    )
                }

            })
        TextField(
            state = nameState, label = { Text("Name") }, modifier = Modifier.fillMaxWidth()
        )
        TextField(
            state = descState, label = { Text("Description") }, modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = timeState,
            onValueChange = {
                if (it.isEmpty() || it.all { c -> c.isDigit() }) {
                    timeState = it
                }
            },
            label = { Text("Minutes per Rep") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )
        LazyRow() {
            items(MuscleGroups.all) { type ->
                FilterChip(selected = type in tags, label = { Text(type) }, onClick = {
                    if (type in tags) {
                        tags -= type
                    } else {
                        tags += type
                    }

                })
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
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) { Text("Loading Exercises...") }
        }

        else -> {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                ListItem(
                    headlineContent = { Text("New Exercise") }, leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Add, contentDescription = "New Exercise"
                        )
                    }, modifier = Modifier.clickable(
                        onClick = {
                            navController.navigate("New Exercise")
                        })
                )
                HorizontalDivider(thickness = 2.dp)
                Spacer(modifier = Modifier.size(12.dp))
                ExerciseList(
                    exercises = exercises, onClick = { exercise ->
                        viewModel.selectExercise(exercise)
                        navController.navigate("Display Exercise")
                    })
            }
        }
    }
}

@Composable
fun ExerciseList(
    exercises: List<Exercise>, onClick: (Exercise) -> Unit
) {
    LazyColumn() {
        items(exercises) { exercise ->
            ExerciseListItem(
                exercise = exercise, onClick = { onClick(exercise) })
        }
    }
}


@Composable
fun WorkoutList(
    workouts: List<Workout>, onClick: (Workout) -> Unit, canOpen: Boolean = true
) {
    LazyColumn() {
        items(workouts) { workout ->
            WorkoutListItem(
                workout = workout, onClick = { onClick(workout) }, canOpen = canOpen
            )
        }
    }
}

@Composable
fun WorkoutListItem(
    workout: Workout, onClick: () -> Unit, canOpen: Boolean = true
) {
    ListItem(headlineContent = {
        Text(
            text = workout.name, fontWeight = FontWeight.SemiBold
        )
    }, leadingContent = {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
    }, supportingContent = {
        Text(
            workout.estimatedMinutes.toString() + " mins: " + workout.exerciseTypeSplit.map { (group, time) -> "$time% $group" })
    }, modifier = Modifier.clickable(onClick = onClick), trailingContent = {
        if (canOpen) {
            Icon(
                imageVector = Icons.Filled.ChevronRight, contentDescription = "Open Workout"
            )
        }
    })
}

@Composable
fun ExerciseListItem(
    exercise: Exercise, onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = exercise.name, fontWeight = FontWeight.SemiBold
            )
        }, leadingContent = {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }, supportingContent = {
            Text(
                "" + exercise.muscleGroups
            )
        }, modifier = Modifier.clickable(onClick = onClick)
    )
}


@Composable
fun DisplayWorkout(
    workout: Workout?, uiState: LibraryUiState, navController: NavController
) {

    var showExercise by remember { mutableStateOf(false) }
    var media = remember { mutableStateListOf("", "") }
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AppHeader(title = workout!!.name, onBack = {
            navController.popBackStack()
        })
        ProfileSectionHeader("Description")
        Text(
            text = workout.description,
            Modifier.padding(horizontal = 16.dp)
        )
        ProfileSectionHeader("Exercises")
        LazyColumn {
            items(workout.exercises) { exercise ->
                ListItem(
                    headlineContent = { Text(exercise.name) },
                    supportingContent = { Text("" + exercise.muscleGroups) },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (exercise.uri != "") {
                                IconButton(
                                    onClick = {
                                        media[0] = exercise.mediaType
                                        media[1] = exercise.uri
                                        showExercise = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Photo,
                                        contentDescription = "Exercise Media"
                                    )
                                }
                            }

                            Text(
                                text = "Reps: " + exercise.reps,
                                fontSize = 16.sp
                            )
                        }
                    }
                )
            }
        }
    }
    if (showExercise) Dialog(
        onDismissRequest = {
            showExercise = false
        },
        content = {
            Log.d("Multimedia", media.toString())
            if (media[0] == "image") {
                AsyncImage(
                    model = media[1],
                    contentDescription = "How to perform exercise",
                    modifier = Modifier
                        .fillMaxWidth()
                        .width(220.dp)
                )
            } else if (media[0] == "video") {
                val context = LocalContext.current
                val exoPlayer = remember {
                    ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(media[1]))
                        prepare()
                        playWhenReady = false
                    }
                }

                DisposableEffect(Unit) {
                    onDispose {
                        exoPlayer.release()
                    }
                }

                AndroidView(
                    factory = {
                        PlayerView(context).apply {
                            player = exoPlayer
                        }
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }
        })
}

@Composable
fun RepsInput(
    value: String, onRepsChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value, onValueChange = {
            if (it.isEmpty() || it.all { c -> c.isDigit() }) {
                onRepsChange(it)
            }
        }, singleLine = true, keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ), modifier = Modifier.size(80.dp, 60.dp), label = { Text("Reps:") })
}

@Composable
fun DisplayExercise(
    exercise: Exercise, onBack: () -> Unit
) {
    Column(
        Modifier.fillMaxSize()
    ) {
        AppHeader(title = exercise.name, onBack = onBack)
        ProfileSectionHeader("Description")
        ListItem(headlineContent = { Text(exercise.description) })
        ProfileSectionHeader("Time per rep (Minutes): ${exercise.estimatedMinutes}")


    }
}

