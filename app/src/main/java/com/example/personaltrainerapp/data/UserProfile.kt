package com.example.personaltrainerapp.data

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val bio: String = "",
    val phoneNumber: String = "",
    val role: UserRole = UserRole.TRAINEE,
    val tags: List<String> = emptyList(),
    val profileImageUrl: String = "",

    val trainerId: String? = null
)