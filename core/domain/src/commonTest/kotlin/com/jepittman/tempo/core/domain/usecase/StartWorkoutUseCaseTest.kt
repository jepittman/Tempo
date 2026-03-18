package com.jepittman.tempo.core.domain.usecase

import com.jepittman.tempo.core.domain.fake.FakeWorkoutRepository
import com.jepittman.tempo.core.domain.model.WorkoutStatus
import com.jepittman.tempo.core.domain.model.WorkoutType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StartWorkoutUseCaseTest {

    private val repository = FakeWorkoutRepository()
    private val useCase = StartWorkoutUseCase(repository)

    @Test
    fun returnsPreparingStatusWithZeroElapsedTime() = runTest {
        val result = useCase(WorkoutType.Running)

        assertEquals(WorkoutStatus.Preparing, result.status)
        assertEquals(0L, result.elapsedSeconds)
        assertNull(result.currentHeartRateBpm)
        assertTrue(result.sets.isEmpty())
    }

    @Test
    fun workoutHasCorrectType() = runTest {
        val result = useCase(WorkoutType.Strength)

        assertEquals(WorkoutType.Strength, result.workout.type)
    }

    @Test
    fun workoutTitleIsPreservedWhenProvided() = runTest {
        val result = useCase(WorkoutType.Strength, title = "Morning Lift")

        assertEquals("Morning Lift", result.workout.title)
    }

    @Test
    fun workoutTitleIsNullWhenNotProvided() = runTest {
        val result = useCase(WorkoutType.Running)

        assertNull(result.workout.title)
    }

    @Test
    fun workoutHasNoEndTimeOnStart() = runTest {
        val result = useCase(WorkoutType.Running)

        assertNull(result.workout.endedAt)
    }

    @Test
    fun workoutIsPersistedInRepository() = runTest {
        val result = useCase(WorkoutType.Cycling)

        val persisted = repository.getWorkout(result.workout.id)
        assertNotNull(persisted)
        assertEquals(WorkoutType.Cycling, persisted.type)
    }
}
