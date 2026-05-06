package com.example.personaltrainerapp.ui.common

import com.google.firebase.auth.FirebaseAuthException

fun friendlyAuthError(e: Exception): String = if (e is FirebaseAuthException) {
    when (e.errorCode) {
        "ERROR_INVALID_CREDENTIAL",
        "ERROR_WRONG_PASSWORD" -> "Incorrect email or password."
        "ERROR_USER_NOT_FOUND" -> "No account found with this email."
        "ERROR_INVALID_EMAIL" -> "Please enter a valid email address."
        "ERROR_USER_DISABLED" -> "This account has been disabled. Please contact support."
        "ERROR_EMAIL_ALREADY_IN_USE" -> "An account with this email already exists."
        "ERROR_WEAK_PASSWORD" -> "Password must be at least 6 characters."
        "ERROR_TOO_MANY_REQUESTS" -> "Too many failed attempts. Please try again later."
        "ERROR_REQUIRES_RECENT_LOGIN" -> "Please log out and log back in before changing your password."
        "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your connection."
        else -> "Something went wrong. Please try again."
    }
} else {
    "Something went wrong. Please try again."
}
