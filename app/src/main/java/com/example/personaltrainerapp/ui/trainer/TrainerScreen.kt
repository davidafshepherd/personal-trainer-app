package com.example.personaltrainerapp.ui.trainer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.personaltrainerapp.data.trainer.TrainerTags
import com.example.personaltrainerapp.data.user.UserProfile
import com.example.personaltrainerapp.ui.common.AppHeader

@Composable
fun TrainerScreen(
    traineeId: String = "demo-user"
) {
    val viewModel = remember { TrainerViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    var isSearching by remember { mutableStateOf(false) }
    var viewedTrainer by remember { mutableStateOf<UserProfile?>(null) }

    LaunchedEffect(traineeId) {
        viewModel.loadTrainers()
        viewModel.loadAssignedTrainer(traineeId)
    }

    when {
        uiState.selectedTrainer != null -> {
            TrainerProfileScreen(
                trainer = uiState.selectedTrainer!!,
                onBack = {
                    isSearching = false
                    viewedTrainer = null
                },
                showRequestButton = false,
                showUnenrolButton = true,
                onRequestTrainer = {},
                onUnenrolTrainer = {
                    viewModel.unenrolFromTrainer(traineeId)
                },
            )
        }

        viewedTrainer != null -> {
            LaunchedEffect(viewedTrainer!!.uid) {
                viewModel.checkPendingRequest(
                    traineeId = traineeId,
                    trainerId = viewedTrainer!!.uid
                )
            }

            val alreadyRequested =
                viewedTrainer!!.uid in uiState.pendingRequestedTrainerIds

            TrainerProfileScreen(
                trainer = viewedTrainer!!,
                onBack = { viewedTrainer = null },
                showRequestButton = true,
                showUnenrolButton = false,
                requestButtonEnabled = !alreadyRequested,
                requestButtonText = if (alreadyRequested) "Already requested" else "Request Trainer",
                onRequestTrainer = {
                    viewModel.requestTrainer(
                        traineeId = traineeId,
                        trainer = viewedTrainer!!
                    )
                    viewedTrainer = null
                    isSearching = false
                },
                onUnenrolTrainer = {}
            )
        }

        isSearching -> {
            TrainerSearchScreen(
                trainers = uiState.trainers,
                isLoading = uiState.isLoading,
                onBack = { isSearching = false },
                onTrainerSelected = { trainer ->
                    viewedTrainer = trainer
                }
            )
        }

        else -> {
            NoTrainerScreen(
                onSearchClick = { isSearching = true }
            )
        }
    }
}

@Composable
private fun NoTrainerScreen(
    onSearchClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppHeader(title = "Trainer")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "It looks like you don't\nhave a trainer yet!",
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text("Search")
            }
        }
    }
}

@Composable
private fun TrainerSearchScreen(
    trainers: List<UserProfile>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onTrainerSelected: (UserProfile) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }

    val tags = TrainerTags.all

    val filteredTrainers = trainers.filter { trainer ->
        val matchesSearch =
            searchText.isBlank() ||
                    trainer.displayName.contains(searchText, ignoreCase = true)

        val matchesTags =
            selectedTags.isEmpty() || selectedTags.all { selectedTag ->
                trainer.tags.contains(selectedTag)
            }

        matchesSearch && matchesTags
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppHeader(
            title = "Available Trainers",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search trainer name") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tags) { tag ->
                FilterChip(
                    selected = tag in selectedTags,
                    onClick = {
                        selectedTags =
                            if (tag in selectedTags) {
                                selectedTags - tag
                            } else {
                                selectedTags + tag
                            }
                    },
                    label = { Text(tag) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> Text("Loading trainers...")
            filteredTrainers.isEmpty() -> Text("No trainers found")

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTrainers) { trainer ->
                        TrainerListItem(
                            trainer = trainer,
                            onClick = { onTrainerSelected(trainer) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrainerListItem(
    trainer: UserProfile,
    onClick: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.padding(6.dp)
            )
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column {
            Text(
                text = trainer.displayName,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = trainer.tags.joinToString(", ") { tag ->
                    tag.replaceFirstChar(Char::uppercase)
                }
            )
        }
    }
}

@Composable
private fun TrainerProfileScreen(
    trainer: UserProfile,
    onBack: () -> Unit,
    showRequestButton: Boolean,
    showUnenrolButton: Boolean,
    onRequestTrainer: () -> Unit,
    onUnenrolTrainer: () -> Unit,
    requestButtonEnabled: Boolean = true,
    requestButtonText: String = "Request Trainer"
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppHeader(
            title = "Trainer",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(24.dp))

        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.size(16.dp))

            Text(
                text = trainer.displayName,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Section(title = "Bio") {
            Text(trainer.bio)
        }

        Section(title = "Contact") {
            Text("Email")
            Text(trainer.email)

            Spacer(modifier = Modifier.height(8.dp))

            Text("Phone Number")
            Text(trainer.phoneNumber)
        }

        Section(title = "Expertise") {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(trainer.tags) { tag ->
                    AssistChip(
                        onClick = {},
                        label = { Text(tag) }
                    )
                }
            }
        }

        if (showRequestButton) {
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onRequestTrainer,
                enabled = requestButtonEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(requestButtonText)
            }
        }

        if (showUnenrolButton) {
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onUnenrolTrainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Unenrol")
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            content()
        }
    }
}