package com.example.personaltrainerapp.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NavigatorTest {

    private lateinit var mockState: NavigationState
    private lateinit var homeStack: NavBackStack<NavKey>
    private lateinit var calendarStack: NavBackStack<NavKey>

    @Before
    fun setUp() {
        mockState = mockk(relaxed = true)
        homeStack = mockk(relaxed = true)
        calendarStack = mockk(relaxed = true)

        every { mockState.startRoute } returns TraineeHomeRoute
        every { mockState.topLevelRoute } returns TraineeHomeRoute
        every { mockState.topLevelRoute = any() } just Runs
        every { mockState.backStacks } returns mapOf(
            TraineeHomeRoute to homeStack,
            TraineeCalendarRoute to calendarStack
        )
    }

    // ── navigate - top-level route ────────────────────────────────────────────

    @Test
    fun `navigate to a top-level route updates topLevelRoute`() {
        val slot = slot<NavKey>()
        every { mockState.topLevelRoute = capture(slot) } just Runs

        Navigator(mockState).navigate(TraineeCalendarRoute)

        assertEquals(TraineeCalendarRoute, slot.captured)
    }

    @Test
    fun `navigate to startRoute keeps topLevelRoute as startRoute`() {
        val slot = slot<NavKey>()
        every { mockState.topLevelRoute = capture(slot) } just Runs

        Navigator(mockState).navigate(TraineeHomeRoute)

        assertEquals(TraineeHomeRoute, slot.captured)
    }

    // ── navigate - non-top-level route ────────────────────────────────────────

    @Test
    fun `navigate to a non-top-level route does not change topLevelRoute`() {
        Navigator(mockState).navigate(TraineeWorkoutsRoute)

        verify(exactly = 0) { mockState.topLevelRoute = any() }
    }

    @Test
    fun `navigate to a non-top-level route adds it to the current stack`() {
        every { mockState.topLevelRoute } returns TraineeHomeRoute

        Navigator(mockState).navigate(TraineeWorkoutsRoute)

        verify { homeStack.add(TraineeWorkoutsRoute) }
    }

    @Test
    fun `navigate to a non-top-level route adds to the active stack not another stack`() {
        every { mockState.topLevelRoute } returns TraineeCalendarRoute

        Navigator(mockState).navigate(TraineeWorkoutsRoute)

        verify { calendarStack.add(TraineeWorkoutsRoute) }
        verify(exactly = 0) { homeStack.add(any()) }
    }

    // ── goBack - at top-level base ────────────────────────────────────────────

    @Test
    fun `goBack at base of a non-start top-level route switches to startRoute`() {
        every { mockState.topLevelRoute } returns TraineeCalendarRoute
        every { mockState.backStacks } returns mapOf(TraineeCalendarRoute to calendarStack)
        // last() delegates to get(size - 1); stub so it returns the top-level route itself
        every { calendarStack.size } returns 1
        every { calendarStack.get(any()) } returns TraineeCalendarRoute

        val slot = slot<NavKey>()
        every { mockState.topLevelRoute = capture(slot) } just Runs

        Navigator(mockState).goBack()

        assertEquals(TraineeHomeRoute, slot.captured)
    }

    // ── goBack - deeper in stack ──────────────────────────────────────────────

    @Test
    fun `goBack from a deeper route pops the stack without changing topLevelRoute`() {
        every { mockState.topLevelRoute } returns TraineeHomeRoute
        every { mockState.backStacks } returns mapOf(TraineeHomeRoute to homeStack)
        // Simulate a deeper entry: last() returns something other than topLevelRoute
        every { homeStack.size } returns 2
        every { homeStack.get(any()) } returns TraineeWorkoutsRoute

        Navigator(mockState).goBack()

        // removeLastOrNull() is an extension that calls removeAt(lastIndex)
        verify { homeStack.removeAt(any()) }
        verify(exactly = 0) { mockState.topLevelRoute = any() }
    }
}
