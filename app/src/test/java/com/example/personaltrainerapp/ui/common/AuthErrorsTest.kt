package com.example.personaltrainerapp.ui.common

import com.google.firebase.auth.FirebaseAuthException
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthErrorsTest {

    private fun firebaseEx(errorCode: String): FirebaseAuthException {
        val ex = mockk<FirebaseAuthException>()
        every { ex.errorCode } returns errorCode
        return ex
    }

    // ── FirebaseAuthException error codes ────────────────────────────────────

    @Test
    fun `ERROR_INVALID_CREDENTIAL returns incorrect credentials message`() {
        assertEquals(
            "Incorrect email or password.",
            friendlyAuthError(firebaseEx("ERROR_INVALID_CREDENTIAL"))
        )
    }

    @Test
    fun `ERROR_WRONG_PASSWORD returns incorrect credentials message`() {
        assertEquals(
            "Incorrect email or password.",
            friendlyAuthError(firebaseEx("ERROR_WRONG_PASSWORD"))
        )
    }

    @Test
    fun `ERROR_USER_NOT_FOUND returns no account message`() {
        assertEquals(
            "No account found with this email.",
            friendlyAuthError(firebaseEx("ERROR_USER_NOT_FOUND"))
        )
    }

    @Test
    fun `ERROR_INVALID_EMAIL returns invalid email message`() {
        assertEquals(
            "Please enter a valid email address.",
            friendlyAuthError(firebaseEx("ERROR_INVALID_EMAIL"))
        )
    }

    @Test
    fun `ERROR_USER_DISABLED returns account disabled message`() {
        assertEquals(
            "This account has been disabled. Please contact support.",
            friendlyAuthError(firebaseEx("ERROR_USER_DISABLED"))
        )
    }

    @Test
    fun `ERROR_EMAIL_ALREADY_IN_USE returns duplicate account message`() {
        assertEquals(
            "An account with this email already exists.",
            friendlyAuthError(firebaseEx("ERROR_EMAIL_ALREADY_IN_USE"))
        )
    }

    @Test
    fun `ERROR_WEAK_PASSWORD returns password too short message`() {
        assertEquals(
            "Password must be at least 6 characters.",
            friendlyAuthError(firebaseEx("ERROR_WEAK_PASSWORD"))
        )
    }

    @Test
    fun `ERROR_TOO_MANY_REQUESTS returns rate limit message`() {
        assertEquals(
            "Too many failed attempts. Please try again later.",
            friendlyAuthError(firebaseEx("ERROR_TOO_MANY_REQUESTS"))
        )
    }

    @Test
    fun `ERROR_REQUIRES_RECENT_LOGIN returns re-login message`() {
        assertEquals(
            "Please log out and log back in before changing your password.",
            friendlyAuthError(firebaseEx("ERROR_REQUIRES_RECENT_LOGIN"))
        )
    }

    @Test
    fun `ERROR_NETWORK_REQUEST_FAILED returns network error message`() {
        assertEquals(
            "Network error. Please check your connection.",
            friendlyAuthError(firebaseEx("ERROR_NETWORK_REQUEST_FAILED"))
        )
    }

    @Test
    fun `unknown FirebaseAuth error code returns generic message`() {
        assertEquals(
            "Something went wrong. Please try again.",
            friendlyAuthError(firebaseEx("ERROR_SOME_UNKNOWN_CODE"))
        )
    }

    // ── Non-Firebase exceptions ──────────────────────────────────────────────

    @Test
    fun `generic RuntimeException returns generic message`() {
        assertEquals(
            "Something went wrong. Please try again.",
            friendlyAuthError(RuntimeException("Something bad"))
        )
    }

    @Test
    fun `plain Exception returns generic message`() {
        assertEquals(
            "Something went wrong. Please try again.",
            friendlyAuthError(Exception("An error occurred"))
        )
    }

    @Test
    fun `IllegalStateException returns generic message`() {
        assertEquals(
            "Something went wrong. Please try again.",
            friendlyAuthError(IllegalStateException("state error"))
        )
    }
}
