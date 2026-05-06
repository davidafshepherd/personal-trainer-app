package com.example.personaltrainerapp.data.trainer

data class TrainerRequest(
    val id: String = "",
    val traineeId: String = "",
    val trainerId: String = "",
    val status: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis()
)