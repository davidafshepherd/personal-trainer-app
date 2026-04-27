package com.example.personaltrainerapp.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object TraineeHomeRoute : NavKey
@Serializable data object TraineeCalendarRoute : NavKey
@Serializable data object TraineeWorkoutsRoute : NavKey
@Serializable data object TraineeTrainerRoute : NavKey
@Serializable data object TraineeProfileRoute : NavKey

@Serializable data object TrainerHomeRoute : NavKey
@Serializable data object TrainerTraineesRoute : NavKey
@Serializable data object TrainerLibraryRoute : NavKey
@Serializable data object TrainerProfileRoute : NavKey
