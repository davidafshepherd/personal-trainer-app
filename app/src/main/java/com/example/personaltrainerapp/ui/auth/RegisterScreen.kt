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
import com.example.personaltrainerapp.data.user.UserProfile
import com.example.personaltrainerapp.data.user.UserRole
import com.example.personaltrainerapp.ui.common.friendlyAuthError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Registration screen, creates a Firebase Auth account and writes the user's profile to Firestore.
 */
@Composable
fun RegisterScreen(
    onGoToLogin: () -> Unit
) {
    // Firebase instances for Firebase Authentication and Firebase Firestore
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    // Form state, role defaults to TRAINEE
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.TRAINEE) }

    // UI state
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Creates a Firebase Auth account, then writes the user's profile to Firestore
    fun register() {
        if (email.isBlank() || password.isBlank()) {
            error = "Email and password are required"
            return
        }
        loading = true
        error = null

        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                val uid = result.user!!.uid

                val userProfile = UserProfile(
                    uid = uid,
                    email = email,
                    displayName = displayName,
                    role = role
                )

                db.collection("users")
                    .document(uid)
                    .set(userProfile)
                    .addOnSuccessListener { loading = false }
                    .addOnFailureListener {
                        error = friendlyAuthError(it)
                        loading = false
                    }
            }
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
            Text("Create Account", style = MaterialTheme.typography.headlineMedium)

            // Display name text field
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

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
                keyboardActions = KeyboardActions(onDone = { register() }),
                modifier = Modifier.fillMaxWidth()
            )

            // Role selector
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                UserRole.entries.forEachIndexed { index, userRole ->
                    SegmentedButton(
                        selected = role == userRole,
                        onClick = { role = userRole },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = UserRole.entries.size
                        ),
                        label = {
                            Text(userRole.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    )
                }
            }

            // Error message, e.g. "The email address is already in use"
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            // Register button
            Button(
                onClick = { register() },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (loading) "Creating account..." else "Create Account")
            }

            // Navigates to LoginScreen for users who already have an account
            TextButton(onClick = onGoToLogin) {
                Text("Already have an account? Login")
            }
        }
    }
}
