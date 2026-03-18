package com.jepittman.tempo.core.domain.usecase

import com.jepittman.tempo.core.domain.fixture.buildActiveWorkout
import com.jepittman.tempo.core.domain.model.WorkoutStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ResumeWorkoutUseCaseTest {

    private val useCase = ResumeWorkoutUseCase()

    @Test
    fun returnsActiveStatus() {
        val result = useCase(buildActiveWorkout(status = WorkoutStatus.Paused))

        assertEquals(WorkoutStatus.Active, result.status)
    }

    @Test
    fun preservesElapsedTime() {
        val paused = buildActiveWorkout(status = WorkoutStatus.Paused, elapsedSeconds = 600L)

        val result = useCase(paused)

        assertEquals(600L, result.elapsedSeconds)
    }

    @Test
    fun throwsWhenWorkoutIsActive() {
        assertFailsWith<IllegalArgumentException> {
            useCase(buildActiveWorkout(status = WorkoutStatus.Active))
        }
    }

    @Test
    fun throwsWhenWorkoutIsCompleted() {
        assertFailsWith<IllegalArgumentException> {
            useCase(buildActiveWorkout(status = WorkoutStatus.Completed))
        }
    }
}
