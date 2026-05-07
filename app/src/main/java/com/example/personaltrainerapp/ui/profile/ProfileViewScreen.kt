package com.example.personaltrainerapp.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.personaltrainerapp.data.user.UserProfile
import com.example.personaltrainerapp.ui.common.AppHeader

@Composable
fun ProfileViewScreen(
    title: String,
    profile: UserProfile,
    onBack: (() -> Unit)? = null,
    primaryButtonText: String? = null,
    primaryButtonEnabled: Boolean = true,
    onPrimaryButtonClick: (() -> Unit)? = null,
    dangerButtonText: String? = null,
    onDangerButtonClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppHeader(
            title = title,
            onBack = onBack
        )

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (profile.profileImageUrl.isBlank()) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile picture",
                    modifier = Modifier.size(96.dp)
                )
            } else {
                AsyncImage(
                    model = profile.profileImageUrl,
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(Modifier.width(16.dp))

            Text(
                text = profile.displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(20.dp))

        Section(title = "Bio") {
            Text(profile.bio.ifBlank { "No bio yet." })
        }

        Section(title = "Contact") {
            Text("Email")
            Text(profile.email)

            Spacer(Modifier.height(8.dp))

            Text("Phone Number")
            Text(profile.phoneNumber.ifBlank { "Not set" })
        }

        Section(title = "Expertise") {
            if (profile.tags.isEmpty()) {
                Text("No expertise added yet.")
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profile.tags.forEach { tag ->
                        AssistChip(
                            onClick = {},
                            label = { Text(tag) }
                        )
                    }
                }
            }
        }

        primaryButtonText?.let {
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { onPrimaryButtonClick?.invoke() },
                enabled = primaryButtonEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(it)
            }
        }

        dangerButtonText?.let {
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { onDangerButtonClick?.invoke() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(it)
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(6.dp))

        content()
    }
}