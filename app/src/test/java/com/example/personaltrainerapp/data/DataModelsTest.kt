package com.example.personaltrainerapp.data

import com.example.personaltrainerapp.data.calendar.AvailabilitySlot
import com.example.personaltrainerapp.data.calendar.AvailabilityStatus
import com.example.personaltrainerapp.data.exercises.MuscleGroups
import com.example.personaltrainerapp.data.notifications.NotificationPreferences
import com.example.personaltrainerapp.data.profile.ProfileTags
import com.example.personaltrainerapp.data.trainer.TrainerRequest
import com.example.personaltrainerapp.data.user.UserProfile
import com.example.personaltrainerapp.data.user.UserRole
import com.example.personaltrainerapp.data.workouts.Exercise
import com.example.personaltrainerapp.data.workouts.Workout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DataModelsTest {

    // ── UserRole ─────────────────────────────────────────────────────────────

    @Test
    fun `UserRole has exactly two values`() {
        assertEquals(2, UserRole.values().size)
    }

    @Test
    fun `UserRole contains TRAINEE`() {
        assertTrue(UserRole.values().contains(UserRole.TRAINEE))
    }

    @Test
    fun `UserRole contains TRAINER`() {
        assertTrue(UserRole.values().contains(UserRole.TRAINER))
    }

    @Test
    fun `UserRole valueOf works for TRAINEE`() {
        assertEquals(UserRole.TRAINEE, UserRole.valueOf("TRAINEE"))
    }

    @Test
    fun `UserRole valueOf works for TRAINER`() {
        assertEquals(UserRole.TRAINER, UserRole.valueOf("TRAINER"))
    }

    // ── UserProfile ──────────────────────────────────────────────────────────

    @Test
    fun `UserProfile default role is TRAINEE`() {
        assertEquals(UserRole.TRAINEE, UserProfile().role)
    }

    @Test
    fun `UserProfile defaults to empty strings`() {
        val profile = UserProfile()
        assertEquals("", profile.uid)
        assertEquals("", profile.email)
        assertEquals("", profile.displayName)
        assertEquals("", profile.bio)
        assertEquals("", profile.phoneNumber)
        assertEquals("", profile.profileImageUrl)
    }

    @Test
    fun `UserProfile default tags is empty list`() {
        assertTrue(UserProfile().tags.isEmpty())
    }

    @Test
    fun `UserProfile default trainerId is null`() {
        assertNull(UserProfile().trainerId)
    }

    @Test
    fun `UserProfile default availability is zero`() {
        assertEquals(0, UserProfile().availability)
    }

    @Test
    fun `UserProfile copy creates a modified independent copy`() {
        val original = UserProfile(uid = "u1", displayName = "Alice")
        val copy = original.copy(displayName = "Bob")

        assertEquals("Alice", original.displayName)
        assertEquals("Bob", copy.displayName)
        assertEquals("u1", copy.uid)
    }

    @Test
    fun `UserProfile equality holds for identical data`() {
        val p1 = UserProfile(uid = "u1", email = "a@b.com", role = UserRole.TRAINER)
        val p2 = UserProfile(uid = "u1", email = "a@b.com", role = UserRole.TRAINER)
        assertEquals(p1, p2)
    }

    @Test
    fun `UserProfile inequality when uid differs`() {
        val p1 = UserProfile(uid = "u1")
        val p2 = UserProfile(uid = "u2")
        assertNotEquals(p1, p2)
    }

    @Test
    fun `UserProfile can store a list of tags`() {
        val tags = listOf("Cardio", "Abs", "Core")
        val profile = UserProfile(tags = tags)
        assertEquals(tags, profile.tags)
    }

    @Test
    fun `UserProfile trainerId can be set and mutated`() {
        val profile = UserProfile(uid = "u1")
        profile.trainerId = "trainer1"
        assertEquals("trainer1", profile.trainerId)
    }

    // ── Exercise ─────────────────────────────────────────────────────────────

    @Test
    fun `Exercise default reps is 8`() {
        assertEquals(8, Exercise().reps)
    }

    @Test
    fun `Exercise default completed is false`() {
        assertFalse(Exercise().completed)
    }

    @Test
    fun `Exercise default muscleGroups is empty`() {
        assertTrue(Exercise().muscleGroups.isEmpty())
    }

    @Test
    fun `Exercise copy with completed toggled`() {
        val exercise = Exercise(id = "e1", completed = false)
        val toggled = exercise.copy(completed = true)
        assertFalse(exercise.completed)
        assertTrue(toggled.completed)
        assertEquals("e1", toggled.id)
    }

    @Test
    fun `Exercise equality holds for identical data`() {
        val e1 = Exercise(id = "e1", name = "Pushup", reps = 10)
        val e2 = Exercise(id = "e1", name = "Pushup", reps = 10)
        assertEquals(e1, e2)
    }

    @Test
    fun `Exercise inequality when id differs`() {
        val e1 = Exercise(id = "e1", name = "Pushup")
        val e2 = Exercise(id = "e2", name = "Pushup")
        assertNotEquals(e1, e2)
    }

    @Test
    fun `Exercise stores muscleGroups correctly`() {
        val groups = listOf("Core", "Legs", "Chest")
        val exercise = Exercise(muscleGroups = groups)
        assertEquals(groups, exercise.muscleGroups)
    }

    @Test
    fun `Exercise default uri is empty string`() {
        assertEquals("", Exercise().uri)
    }

    @Test
    fun `Exercise default mediaType is empty string`() {
        assertEquals("", Exercise().mediaType)
    }

    // ── Workout ──────────────────────────────────────────────────────────────

    @Test
    fun `Workout default exercises is empty list`() {
        assertTrue(Workout().exercises.isEmpty())
    }

    @Test
    fun `Workout default estimatedMinutes is zero`() {
        assertEquals(0, Workout().estimatedMinutes)
    }

    @Test
    fun `Workout default exerciseTypeSplit is empty map`() {
        assertTrue(Workout().exerciseTypeSplit.isEmpty())
    }

    @Test
    fun `Workout copy produces modified independent copy`() {
        val original = Workout(id = "w1", name = "Leg Day")
        val copy = original.copy(name = "Arm Day")
        assertEquals("Leg Day", original.name)
        assertEquals("Arm Day", copy.name)
        assertEquals("w1", copy.id)
    }

    @Test
    fun `Workout equality holds for identical data`() {
        val w1 = Workout(id = "w1", name = "Leg Day", trainerId = "t1")
        val w2 = Workout(id = "w1", name = "Leg Day", trainerId = "t1")
        assertEquals(w1, w2)
    }

    @Test
    fun `Workout with exercises preserves exercise list`() {
        val exercises = listOf(
            Exercise(id = "e1", name = "Squat"),
            Exercise(id = "e2", name = "Lunge")
        )
        val workout = Workout(id = "w1", exercises = exercises)
        assertEquals(2, workout.exercises.size)
        assertEquals("e1", workout.exercises[0].id)
    }

    @Test
    fun `Workout copy does not mutate original exercise list`() {
        val exercise = Exercise(id = "e1", completed = false)
        val original = Workout(id = "w1", exercises = listOf(exercise))
        val modified = original.copy(exercises = listOf(exercise.copy(completed = true)))

        assertFalse(original.exercises.first().completed)
        assertTrue(modified.exercises.first().completed)
    }

    // ── AvailabilityStatus ───────────────────────────────────────────────────

    @Test
    fun `AvailabilityStatus has exactly three values`() {
        assertEquals(3, AvailabilityStatus.values().size)
    }

    @Test
    fun `AvailabilityStatus contains IDEAL MAYBE and BUSY`() {
        val statuses = AvailabilityStatus.values().toSet()
        assertTrue(statuses.contains(AvailabilityStatus.IDEAL))
        assertTrue(statuses.contains(AvailabilityStatus.MAYBE))
        assertTrue(statuses.contains(AvailabilityStatus.BUSY))
    }

    @Test
    fun `AvailabilityStatus valueOf is correct`() {
        assertEquals(AvailabilityStatus.IDEAL, AvailabilityStatus.valueOf("IDEAL"))
        assertEquals(AvailabilityStatus.MAYBE, AvailabilityStatus.valueOf("MAYBE"))
        assertEquals(AvailabilityStatus.BUSY, AvailabilityStatus.valueOf("BUSY"))
    }

    // ── AvailabilitySlot ─────────────────────────────────────────────────────

    @Test
    fun `AvailabilitySlot default status is IDEAL`() {
        assertEquals(AvailabilityStatus.IDEAL, AvailabilitySlot().status)
    }

    @Test
    fun `AvailabilitySlot default hour is zero`() {
        assertEquals(0, AvailabilitySlot().hour)
    }

    @Test
    fun `AvailabilitySlot copy updates status`() {
        val slot = AvailabilitySlot(id = "s1", status = AvailabilityStatus.IDEAL)
        val updated = slot.copy(status = AvailabilityStatus.BUSY)
        assertEquals(AvailabilityStatus.BUSY, updated.status)
        assertEquals(AvailabilityStatus.IDEAL, slot.status)
    }

    @Test
    fun `AvailabilitySlot equality holds for identical data`() {
        val date = LocalDate.of(2026, 5, 10)
        val s1 = AvailabilitySlot("id1", "trainee1", date, 9, AvailabilityStatus.IDEAL)
        val s2 = AvailabilitySlot("id1", "trainee1", date, 9, AvailabilityStatus.IDEAL)
        assertEquals(s1, s2)
    }

    // ── TrainerRequest ───────────────────────────────────────────────────────

    @Test
    fun `TrainerRequest default status is PENDING`() {
        assertEquals("PENDING", TrainerRequest().status)
    }

    @Test
    fun `TrainerRequest defaults to empty strings`() {
        val request = TrainerRequest()
        assertEquals("", request.id)
        assertEquals("", request.traineeId)
        assertEquals("", request.trainerId)
    }

    @Test
    fun `TrainerRequest equality holds for identical data`() {
        val r1 = TrainerRequest(id = "r1", traineeId = "t1", trainerId = "tr1", status = "PENDING")
        val r2 = TrainerRequest(id = "r1", traineeId = "t1", trainerId = "tr1", status = "PENDING")
        assertEquals(r1, r2)
    }

    @Test
    fun `TrainerRequest inequality when status differs`() {
        val r1 = TrainerRequest(id = "r1", status = "PENDING")
        val r2 = TrainerRequest(id = "r1", status = "ACCEPTED")
        assertNotEquals(r1, r2)
    }

    // ── MuscleGroups ─────────────────────────────────────────────────────────

    @Test
    fun `MuscleGroups has seven entries`() {
        assertEquals(7, MuscleGroups.all.size)
    }

    @Test
    fun `MuscleGroups contains Abs`() {
        assertTrue(MuscleGroups.all.contains("Abs"))
    }

    @Test
    fun `MuscleGroups contains Core`() {
        assertTrue(MuscleGroups.all.contains("Core"))
    }

    @Test
    fun `MuscleGroups contains Cardio`() {
        assertTrue(MuscleGroups.all.contains("Cardio"))
    }

    @Test
    fun `MuscleGroups contains Chest`() {
        assertTrue(MuscleGroups.all.contains("Chest"))
    }

    @Test
    fun `MuscleGroups contains Legs`() {
        assertTrue(MuscleGroups.all.contains("Legs"))
    }

    @Test
    fun `MuscleGroups contains Biceps`() {
        assertTrue(MuscleGroups.all.contains("Biceps"))
    }

    @Test
    fun `MuscleGroups contains Triceps`() {
        assertTrue(MuscleGroups.all.contains("Triceps"))
    }

    @Test
    fun `MuscleGroups has no duplicate entries`() {
        assertEquals(MuscleGroups.all.size, MuscleGroups.all.toSet().size)
    }

    // ── ProfileTags ───────────────────────────────────────────────────────────

    @Test
    fun `ProfileTags has nine entries`() {
        assertEquals(9, ProfileTags.all.size)
    }

    @Test
    fun `ProfileTags contains all expected goal tags`() {
        val tags = ProfileTags.all
        assertTrue(tags.contains("cardio"))
        assertTrue(tags.contains("strength"))
        assertTrue(tags.contains("flexibility"))
        assertTrue(tags.contains("endurance"))
        assertTrue(tags.contains("weight loss"))
        assertTrue(tags.contains("muscle gain"))
        assertTrue(tags.contains("mobility"))
        assertTrue(tags.contains("nutrition"))
        assertTrue(tags.contains("rehab"))
    }

    @Test
    fun `ProfileTags has no duplicate entries`() {
        assertEquals(ProfileTags.all.size, ProfileTags.all.toSet().size)
    }

    // ── NotificationPreferences ───────────────────────────────────────────────

    @Test
    fun `NotificationPreferences default emailWorkoutScheduled is true`() {
        assertTrue(NotificationPreferences().emailWorkoutScheduled)
    }

    @Test
    fun `NotificationPreferences default pushWorkoutScheduled is true`() {
        assertTrue(NotificationPreferences().pushWorkoutScheduled)
    }

    @Test
    fun `NotificationPreferences equality holds for identical data`() {
        val p1 = NotificationPreferences(emailWorkoutScheduled = false, pushWorkoutScheduled = true)
        val p2 = NotificationPreferences(emailWorkoutScheduled = false, pushWorkoutScheduled = true)
        assertEquals(p1, p2)
    }

    @Test
    fun `NotificationPreferences inequality when fields differ`() {
        val p1 = NotificationPreferences(emailWorkoutScheduled = true, pushWorkoutScheduled = true)
        val p2 = NotificationPreferences(emailWorkoutScheduled = false, pushWorkoutScheduled = true)
        assertNotEquals(p1, p2)
    }

    @Test
    fun `NotificationPreferences copy updates email flag independently`() {
        val original = NotificationPreferences(emailWorkoutScheduled = true, pushWorkoutScheduled = true)
        val updated = original.copy(emailWorkoutScheduled = false)
        assertTrue(original.emailWorkoutScheduled)
        assertFalse(updated.emailWorkoutScheduled)
        assertTrue(updated.pushWorkoutScheduled)
    }

    @Test
    fun `NotificationPreferences copy updates push flag independently`() {
        val original = NotificationPreferences(emailWorkoutScheduled = true, pushWorkoutScheduled = true)
        val updated = original.copy(pushWorkoutScheduled = false)
        assertTrue(original.pushWorkoutScheduled)
        assertFalse(updated.pushWorkoutScheduled)
        assertTrue(updated.emailWorkoutScheduled)
    }
}
