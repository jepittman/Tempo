package com.jepittman.tempo.core.domain.usecase

import com.jepittman.tempo.core.domain.fake.FakeWorkoutRepository
import com.jepittman.tempo.core.domain.fixture.buildActiveWorkout
import com.jepittman.tempo.core.domain.model.WorkoutStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DiscardWorkoutUseCaseTest {

    private val repository = FakeWorkoutRepository()
    private val useCase = DiscardWorkoutUseCase(repository)

    @Test
    fun setsStatusToDiscarded() = runTest {
        val active = buildActiveWorkout()
        repository.saveWorkout(active.workout)

        val result = useCase(active)

        assertEquals(WorkoutStatus.Discarded, result.status)
    }

    @Test
    fun removesWorkoutFromRepository() = runTest {
        val active = buildActiveWorkout()
        repository.saveWorkout(active.workout)

        useCase(active)

        assertNull(repository.getWorkout(active.workout.id))
    }
}
