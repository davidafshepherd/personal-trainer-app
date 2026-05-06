package com.example.personaltrainerapp.data.profile

import android.net.Uri
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Handles profile-related Firebase operations.
 */
class ProfileRepository {

    // Firebase instances for Firebase Authentication, Firebase Firestore and Firebase Storage
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // UID of the currently signed-in user
    private val uid get() = requireNotNull(auth.currentUser?.uid) { "No signed-in user" }

    // Updates a single field on the signed-in user's Firestore document
    suspend fun updateField(field: String, value: Any) {
        db.collection("users").document(uid).update(field, value).await()
    }

    // Uploads a profile image and returns its download URL
    suspend fun uploadProfileImage(uri: Uri): String {
        val ref = storage.reference.child("profile_images/$uid.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    // Re-authenticates the user before updating their password
    suspend fun changePassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser ?: error("No signed-in user")
        val email = user.email ?: error("User has no email address")
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential).await()
        user.updatePassword(newPassword).await()
    }
}
