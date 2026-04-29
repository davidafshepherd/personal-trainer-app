package com.example.personaltrainerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.personaltrainerapp.data.user.UserProfile
import com.example.personaltrainerapp.data.user.UserRole
import com.example.personaltrainerapp.ui.app.AppFrame
import com.example.personaltrainerapp.ui.theme.PersonalTrainerAppTheme


val user = UserProfile(
    uid = "01",
    email = "johndoe@gmail.com",
    displayName = "John Doe",
    bio = "Sup",
    phoneNumber = "07518 542109",
    role = UserRole.TRAINEE,
    tags = listOf("cardio", "strength", "endurance"),
    profileImageUrl = "",
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonalTrainerAppTheme {
                AppFrame(user)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PersonalTrainerAppTheme {
        AppFrame(user)
    }
}
