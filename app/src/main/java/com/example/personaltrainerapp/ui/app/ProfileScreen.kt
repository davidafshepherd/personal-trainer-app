package com.example.personaltrainerapp.ui.app

import androidx.compose.runtime.Composable
import com.example.personaltrainerapp.data.user.UserRole

@Composable
fun ProfileScreen(role: UserRole) {
    BlankScreen(
        title = if (role == UserRole.TRAINER) "Trainer Profile" else "Trainee Profile"
    )
}
