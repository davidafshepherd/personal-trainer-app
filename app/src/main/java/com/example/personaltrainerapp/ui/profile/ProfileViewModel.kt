package com.example.personaltrainerapp.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltrainerapp.data.profile.ProfileRepository
import com.example.personaltrainerapp.data.user.UserProfile
import com.example.personaltrainerapp.data.user.UserRole
import com.example.personaltrainerapp.ui.common.friendlyAuthError
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isUploadingImage: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

/**
 * Manages profile state and profile-related Firebase updates.
 */
class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    // Profile state
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Initialises profile state once from the loaded user profile
    fun seed(initial: UserProfile) {
        if (_uiState.value.profile == null) {
            _uiState.value = _uiState.value.copy(profile = initial)
        }
    }

    // Updates the user's display name
    fun updateDisplayName(name: String) =
        persistField("displayName", name, "Display name updated.") { it.copy(displayName = name) }

    // Updates the user's bio
    fun updateBio(bio: String) =
        persistField("bio", bio, "Bio updated.") { it.copy(bio = bio) }

    // Updates the user's phone number
    fun updatePhoneNumber(phone: String) =
        persistField("phoneNumber", phone, "Phone number updated.") { it.copy(phoneNumber = phone) }

    // Updates user's goal/expertise tags
    fun updateTags(tags: List<String>) {
        val label = if (_uiState.value.profile?.role == UserRole.TRAINER) "Expertise" else "Goals"
        persistField("tags", tags, "$label updated.") { it.copy(tags = tags) }
    }

    // Clears the current success message
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    // Uploads a new profile image and stores its URL
    fun uploadProfileImage(uri: Uri) {
        val previous = _uiState.value.profile ?: return
        _uiState.value = _uiState.value.copy(isUploadingImage = true, error = null, message = null)

        viewModelScope.launch {
            try {
                val url = repository.uploadProfileImage(uri)
                repository.updateField("profileImageUrl", url)
                _uiState.value = _uiState.value.copy(
                    profile = previous.copy(profileImageUrl = url),
                    isUploadingImage = false,
                    message = "Profile picture updated."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploadingImage = false,
                    error = "Failed to upload image: ${e.message}"
                )
            }
        }
    }

    // Change's the user's password
    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _uiState.value = _uiState.value.copy(message = null)
        viewModelScope.launch {
            try {
                repository.changePassword(currentPassword, newPassword)
                _uiState.value = _uiState.value.copy(
                    message = "Password updated successfully.",
                    error = null
                )
                onSuccess()
            } catch (e: Exception) {
                val msg = if (e is FirebaseAuthException &&
                    e.errorCode in listOf("ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD")
                ) "Incorrect password." else friendlyAuthError(e)
                onError(msg)
            }
        }
    }

    // Applies a change locally for instant feedback, then persists to Firestore.
    // Reverts the local state if the write fails.
    private fun persistField(field: String, value: Any, successMessage: String = "", apply: (UserProfile) -> UserProfile) {
        val previous = _uiState.value.profile ?: return
        _uiState.value = _uiState.value.copy(profile = apply(previous), error = null, message = null)

        viewModelScope.launch {
            try {
                repository.updateField(field, value)
                if (successMessage.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(message = successMessage)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(profile = previous, error = e.message)
            }
        }
    }
}
