package com.example.personaltrainerapp.ui.trainees

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.personaltrainerapp.data.user.UserProfile
import com.example.personaltrainerapp.ui.common.AppHeader

@Composable
@Preview(showBackground = true)
fun TraineesScreen(
    trainerId: String = "demo-user",

    ) {

    val navController = rememberNavController()
    val viewModel = remember { TraineeViewModel() }
    val uiState by viewModel.uiState.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.loadTrainees(trainerId)
    }

    NavHost(navController, startDestination = "Trainees") {
        composable("Trainees") {
            MyTraineesScreen(
                isLoading = uiState.isLoading,
                myTrainees = uiState.myTrainees,
                viewTrainee = { trainee ->
                    viewModel.selectTrainee(trainee.uid)
                    navController.navigate("selectedTrainee")
                }
            )
        }
        composable("selectedTrainee") {
            DisplayTrainee(
                trainee = uiState.selectedTrainee!!,
                onBack = {navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun DisplayTrainee(
    trainee: UserProfile,
    onBack: () -> Unit
){
    Column{
        AppHeader(
            title = trainee.displayName,
            onBack = onBack
        )


    }

}

@Composable
private fun MyTraineesScreen(
    isLoading: Boolean,
    myTrainees: List<UserProfile>,
    viewTrainee: (UserProfile) -> Unit
) {
    var menuExpanded = false
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppHeader(
            title = "Trainees",
            trailingContent = {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Set Availability") },
                        onClick = {
                            menuExpanded = false
                            // trigger navigation
                        }
                    )
                }
            }
            )
        when {
            isLoading -> {
                Text("Loading Trainees...")
            }

            myTrainees.isEmpty() -> {
                Text("You have no trainees")
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(myTrainees) { trainee ->
                        TraineeListItem(
                            trainee = trainee,
                            onClick = { viewTrainee(trainee) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TraineeListItem(
    trainee: UserProfile,
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
                text = trainee.displayName,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = trainee.tags.joinToString(", ") { tag ->
                    tag.replaceFirstChar(Char::uppercase)
                }
            )
        }
    }
}