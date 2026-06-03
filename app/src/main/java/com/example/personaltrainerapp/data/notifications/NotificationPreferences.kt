package com.example.personaltrainerapp.data.notifications

data class NotificationPreferences(
    val emailWorkoutScheduled: Boolean = true,
    val pushWorkoutScheduled: Boolean = true
)