package com.example.personaltrainerapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.personaltrainerapp.ui.common.friendlyAuthError
import com.google.firebase.auth.FirebaseAuth

/**
 * Login screen, signs the user in with email and password.
 */
@Composable
fun LoginScreen(
    onGoToRegister: () -> Unit
) {
    // Firebase instance for Firebase Authentication
    val auth = remember { FirebaseAuth.getInstance() }

    // Form state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // UI state
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Signs the user in with email and password
    fun login() {
        if (email.isBlank() || password.isBlank()) {
            error = "Email and password are required"
            return
        }
        loading = true
        error = null

        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { loading = false }
            .addOnFailureListener {
                error = friendlyAuthError(it)
                loading = false
            }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Login", style = MaterialTheme.typography.headlineMedium)

            // Email text field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Password text field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { login() }),
                modifier = Modifier.fillMaxWidth()
            )

            // Error message, e.g. "Wrong password or account not found"
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            // Login button
            Button(
                onClick = { login() },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (loading) "Signing in..." else "Login")
            }

            // Navigates to RegisterScreen for users who have no account
            TextButton(onClick = onGoToRegister) {
                Text("Don't have an account? Register")
            }
        }
    }
}
