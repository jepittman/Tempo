package com.jepittman.tempo.core.domain.usecase

import com.jepittman.tempo.core.domain.fixture.buildActiveWorkout
import com.jepittman.tempo.core.domain.model.WorkoutStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PauseWorkoutUseCaseTest {

    private val useCase = PauseWorkoutUseCase()

    @Test
    fun returnsPausedStatus() {
        val result = useCase(buildActiveWorkout(status = WorkoutStatus.Active))

        assertEquals(WorkoutStatus.Paused, result.status)
    }

    @Test
    fun preservesElapsedTimeAndHeartRate() {
        val active = buildActiveWorkout(elapsedSeconds = 300L, currentHeartRateBpm = 150)

        val result = useCase(active)

        assertEquals(300L, result.elapsedSeconds)
        assertEquals(150, result.currentHeartRateBpm)
    }

    @Test
    fun throwsWhenWorkoutIsAlreadyPaused() {
        assertFailsWith<IllegalArgumentException> {
            useCase(buildActiveWorkout(status = WorkoutStatus.Paused))
        }
    }

    @Test
    fun throwsWhenWorkoutIsCompleted() {
        assertFailsWith<IllegalArgumentException> {
            useCase(buildActiveWorkout(status = WorkoutStatus.Completed))
        }
    }
}
