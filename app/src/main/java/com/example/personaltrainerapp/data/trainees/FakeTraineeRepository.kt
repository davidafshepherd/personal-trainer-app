package com.example.personaltrainerapp.data.trainees

import com.example.personaltrainerapp.data.user.UserProfile
import com.example.personaltrainerapp.data.user.UserRole
import com.google.firebase.firestore.auth.User

class FakeTraineeRepository {

    private var trainees = listOf(
        UserProfile("1", "example1@gmail.com", "User 1", bio = "I am User 1", "01111 555 111",UserRole.TRAINEE, listOf("Abs", "Cardio", "Core"), "", "demo-user"),
        UserProfile("2", "example2@gmail.com", "User 2", bio = "I am User 2", "02222 555 222",UserRole.TRAINEE, listOf("Abs", "Cardio", "Core"), "", "demo-user"),
        UserProfile("3", "example3@gmail.com", "User 3", bio = "I am User 3", "03333 555 333",UserRole.TRAINEE, listOf("Abs", "Cardio", "Core"), "", "demo-user"),
        UserProfile("4", "example4@gmail.com", "User 4", bio = "I am User 4", "04444 555 444",UserRole.TRAINEE, listOf("Abs", "Cardio", "Core"), "", "demo-user"),
        UserProfile("5", "example5@gmail.com", "User 5", bio = "I am User 5", "05555 555 555",UserRole.TRAINEE, listOf("Abs", "Cardio", "Core"), "", "demo-user"),
        UserProfile("6", "example6@gmail.com", "User 6", bio = "I am User 6", "06666 555 666",UserRole.TRAINEE, listOf("Abs", "Cardio", "Core"), "", "demo-user"),
        UserProfile("7", "example7@gmail.com", "User 7", bio = "I am User 7", "07777 555 777",UserRole.TRAINEE, listOf("Abs", "Cardio", "Core"), "", "demo-user")
    )


    suspend fun getAllTrainees(): List<UserProfile>{
        return trainees
    }

    suspend fun getTraineeById(traineeId: String): UserProfile? {
        return trainees.find { (uid, _, _, _, _, _, _, _, _) -> uid == traineeId }
    }

    suspend fun updateTrainer(traineeId: String, trainerId:String) {
        getTraineeById(traineeId)?.trainerId = trainerId
    }

}