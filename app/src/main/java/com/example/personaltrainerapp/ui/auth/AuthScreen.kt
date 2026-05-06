package com.example.personaltrainerapp.ui.auth

import androidx.compose.runtime.*

/**
 * Entry point for unauthenticated users.
 */
@Composable
fun AuthScreen() {
    // When true, show RegisterScreen. When false, show LoginScreen.
    var showRegister by remember { mutableStateOf(false) }

    if (showRegister) {
        RegisterScreen(
            onGoToLogin = { showRegister = false }
        )
    } else {
        LoginScreen(
            onGoToRegister = { showRegister = true }
        )
    }
}
