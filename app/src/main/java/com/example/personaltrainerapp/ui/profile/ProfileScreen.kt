package com.example.personaltrainerapp.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import coil3.compose.AsyncImage
import com.example.personaltrainerapp.data.profile.ProfileTags
import com.example.personaltrainerapp.data.user.UserProfile
import com.example.personaltrainerapp.data.user.UserRole
import com.example.personaltrainerapp.ui.common.AppHeader
import com.google.firebase.auth.FirebaseAuth

/**
 * Profile screen, displays and edits the signed-in user's profile.
 */
@Composable
fun ProfileScreen(user: UserProfile, modifier: Modifier = Modifier) {
    // Firebase instance for Firebase Authentication + Profile View Model
    val auth = remember { FirebaseAuth.getInstance() }
    val viewModel = viewModel<ProfileViewModel>()

    // Seed with the profile already loaded by RootScreen on first composition
    LaunchedEffect(user.uid) { viewModel.seed(user) }

    // Clear any lingering success message when the user navigates away from this screen
    DisposableEffect(Unit) { onDispose { viewModel.clearMessage() } }

    // Observe UI state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.profile

    // Auto-dismiss success messages after 5 seconds
    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            delay(5000)
            viewModel.clearMessage()
        }
    }

    // Controls which dialog is currently open
    var editingDisplayName by remember { mutableStateOf(false) }
    var editingBio by remember { mutableStateOf(false) }
    var editingPhone by remember { mutableStateOf(false) }
    var editingPassword by remember { mutableStateOf(false) }
    var editingTags by remember { mutableStateOf(false) }

    // Launches the system photo picker, result URI is passed to the ViewModel for upload
    val imageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.uploadProfileImage(it) }
    }

    // Dialogs rendered as overlays on top of the screen
    if (editingDisplayName) {
        EditTextDialog(
            title = "Display Name",
            initial = profile?.displayName ?: "",
            onSave = { viewModel.updateDisplayName(it); editingDisplayName = false },
            onDismiss = { editingDisplayName = false }
        )
    }
    if (editingBio) {
        EditTextDialog(
            title = "Bio",
            initial = profile?.bio ?: "",
            singleLine = false,
            onSave = { viewModel.updateBio(it); editingBio = false },
            onDismiss = { editingBio = false }
        )
    }
    if (editingPhone) {
        EditTextDialog(
            title = "Phone Number",
            initial = profile?.phoneNumber ?: "",
            onSave = { viewModel.updatePhoneNumber(it); editingPhone = false },
            onDismiss = { editingPhone = false }
        )
    }
    if (editingPassword) {
        ChangePasswordDialog(
            onSave = { current, new, onError ->
                viewModel.changePassword(
                    current, new,
                    onSuccess = { editingPassword = false },
                    onError = onError
                )
            },
            onDismiss = { editingPassword = false }
        )
    }
    if (editingTags) {
        val label = if (profile?.role == UserRole.TRAINER) "Expertise" else "Goals"
        EditTagsDialog(
            title = label,
            selected = profile?.tags ?: emptyList(),
            onSave = { viewModel.updateTags(it); editingTags = false },
            onDismiss = { editingTags = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // AppHeader
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            AppHeader(title = "Profile")
        }

        // Profile image + display name
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clickable {
                        imageLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // If user has no profile image, display default profile icon
                // Otherwise, the user's profile image
                if (profile?.profileImageUrl.isNullOrBlank()) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile picture",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = profile?.profileImageUrl,
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }

                // If upload in progress, display a circular progress indicator
                // Otherwise, display an edit icon
                if (uiState.isUploadingImage) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(22.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .padding(3.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // Display name
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Display name
                Text(
                    text = profile?.displayName ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(Modifier.width(4.dp))

                // Edit icon
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit display name",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { editingDisplayName = true }
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Bio section
        ProfileSectionHeader(title = "Bio", onEdit = { editingBio = true })
        Text(
            text = profile?.bio?.ifBlank { "No bio yet." } ?: "No bio yet.",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        // Personal section (email, phone number and password)
        ProfileSectionHeader(title = "Personal")
        ProfileField(
            label = "Email",
            value = profile?.email ?: ""
        )
        ProfileField(
            label = "Phone Number",
            value = profile?.phoneNumber?.ifBlank { "Not set" } ?: "Not set",
            onEdit = { editingPhone = true }
        )
        ProfileField(
            label = "Password",
            value = "••••••••",
            onEdit = { editingPassword = true },
            showDivider = false
        )

        // Goals section for trainees, Expertise section for trainers
        val tagsLabel = if (profile?.role == UserRole.TRAINER) "Expertise" else "Goals"
        ProfileSectionHeader(title = tagsLabel, onEdit = { editingTags = true })
        if (profile?.tags.isNullOrEmpty()) {
            Text(
                text = "No ${tagsLabel.lowercase()} added yet.",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                profile?.tags?.forEach { tag ->
                    AssistChip(onClick = {}, label = { Text(tag) })
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Logout button
        Button(
            onClick = { auth.signOut() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Logout")
        }

        // Error message
        uiState.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp)
            )
        }

        // Success message, auto-dismissed after 5 seconds
        uiState.message?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

/**
 * Section header with an optional inline edit icon.
 */
@Composable
fun ProfileSectionHeader(
    title: String,
    onEdit: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(
                start = 20.dp,
                end = if (onEdit != null) 12.dp else 20.dp,
                top = 4.dp,
                bottom = 4.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Section title
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        // If section is editable, display an edit icon
        if (onEdit != null) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit $title",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onEdit() }
            )
        }
    }
}

/**
 * Section field with an optional edit icon and divider.
 */
@Composable
private fun ProfileField(
    label: String,
    value: String,
    onEdit: (() -> Unit)? = null,
    showDivider: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 20.dp,
                end = if (onEdit != null) 8.dp else 20.dp,
                top = 6.dp,
                bottom = 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Field name
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
        // If field is editable, display an edit icon
        if (onEdit != null) {
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit $label",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
    // Field divider
    if (showDivider) {
        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
    }
}

/**
 * Text field edit dialog, used for display name, bio and phone number.
 */
@Composable
private fun EditTextDialog(
    title: String,
    initial: String,
    singleLine: Boolean = true,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Seeded from initial (resets if initial changes after a successful save)
    var value by remember(initial) { mutableStateOf(initial) }

    // Text field edit dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            // Text field (multi-line for bio, single-line for everything else)
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = singleLine,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (!singleLine) Modifier.height(120.dp) else Modifier)
            )
        },
        // Save button
        confirmButton = {
            TextButton(onClick = { onSave(value.trim()) }) { Text("Save") }
        },
        // Cancel button
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Password edit dialog.
 */
@Composable
private fun ChangePasswordDialog(
    onSave: (currentPassword: String, newPassword: String, onError: (String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    // Dialog state (error is shown inline and cleared on any keystroke)
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    // Password edit dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Current password text field
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it; error = null },
                    label = { Text("Current Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                // New password text field
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; error = null },
                    label = { Text("New Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onSave(currentPassword, newPassword) { error = it } }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                // Error message
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        // Update button
        confirmButton = {
            TextButton(
                onClick = { onSave(currentPassword, newPassword) { error = it } },
                enabled = currentPassword.isNotBlank() && newPassword.isNotBlank()
            ) {
                Text("Update")
            }
        },
        // Cancel button
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Goal/Expertise tag edit dialog.
 */
@Composable
private fun EditTagsDialog(
    title: String,
    selected: List<String>,
    onSave: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    // Currently selected goal/expertise tags
    var current by remember { mutableStateOf(selected.toSet()) }

    // Goal/expertise tag edit dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Instruction text
                Text(
                    text = "Select all that apply:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Set of goal/expertise tags to select from
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ProfileTags.all.forEach { tag ->
                        FilterChip(
                            selected = tag in current,
                            onClick = {
                                current = if (tag in current) current - tag else current + tag
                            },
                            label = { Text(tag) }
                        )
                    }
                }
            }
        },
        // Save button
        confirmButton = {
            TextButton(onClick = { onSave(current.toList()) }) { Text("Save") }
        },
        // Cancel button
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
