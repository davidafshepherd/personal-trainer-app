package com.example.personaltrainerapp.ui.app

import androidx.compose.runtime.*
import com.example.personaltrainerapp.data.notifications.NotificationRepository
import com.example.personaltrainerapp.data.user.UserProfile
import com.example.personaltrainerapp.ui.auth.AuthScreen
import com.example.personaltrainerapp.ui.common.LoadingScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Top-level router, decides which screen to show based on auth and profile state:
 *    Loading - waiting for Firebase to report the current user or fetching their Firestore profile;
 *    AuthScreen - user is not signed in (or the profile fetch failed);
 *    AppFrame - user is signed in and their profile has loaded successfully.
 */
@Composable
fun RootScreen() {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    // The currently signed-in Firebase user (null when no one is logged in)
    var firebaseUser by remember { mutableStateOf(auth.currentUser) }
    // The Firestore profile document for the signed-in user (contains role, display name, etc.)
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    // True while we are waiting for Firebase Auth or Firestore to respond
    var loading by remember { mutableStateOf(true) }
    val notificationRepository = remember { NotificationRepository() }

    // Keep firebaseUser in sync with Firebase Auth
    // Fires immediately on registration and again on every subsequent sign-in or sign-out
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            firebaseUser = firebaseAuth.currentUser
        }

        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    // Re-runs whenever the signed-in user changes (new login, logout or account switch)
    LaunchedEffect(firebaseUser?.uid) {
        val uid = firebaseUser?.uid

        // No user signed in - clear any stale profile and stop loading
        if (uid == null) {
            profile = null
            loading = false
            return@LaunchedEffect
        }

        loading = true

        // Fetch the user's Firestore profile document using their Firebase Auth UID
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                profile = document.toObject(UserProfile::class.java)
                loading = false
            }
            .addOnFailureListener {
                profile = null
                loading = false
            }
    }

    LaunchedEffect(profile?.uid) {
        val uid = profile?.uid

        if (!uid.isNullOrBlank()) {
            notificationRepository.saveFcmToken(uid)
        }
    }

    // Show the correct screen based on the current auth and profile state
    when {
        loading -> LoadingScreen()
        firebaseUser == null -> AuthScreen()
        profile != null -> AppFrame(profile!!)
        else  -> AuthScreen()
    }
}
