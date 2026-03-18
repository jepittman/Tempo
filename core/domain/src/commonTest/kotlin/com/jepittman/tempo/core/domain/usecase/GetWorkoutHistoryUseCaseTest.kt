package com.jepittman.tempo.core.domain.usecase

import com.jepittman.tempo.core.domain.fake.FakeWorkoutRepository
import com.jepittman.tempo.core.domain.model.WorkoutType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetWorkoutHistoryUseCaseTest {

    private val repository = FakeWorkoutRepository()
    private val useCase = GetWorkoutHistoryUseCase(repository)

    @Test
    fun emitsEmptyListWhenNoWorkoutsExist() = runTest {
        val summaries = useCase().first()

        assertTrue(summaries.isEmpty())
    }

    @Test
    fun emitsSummaryForEachWorkout() = runTest {
        repository.createWorkout(WorkoutType.Running)
        repository.createWorkout(WorkoutType.Cycling)

        val summaries = useCase().first()

        assertEquals(2, summaries.size)
    }

    @Test
    fun emitsUpdatedListWhenWorkoutIsAdded() = runTest {
        val flow = useCase()

        assertTrue(flow.first().isEmpty())

        repository.createWorkout(WorkoutType.Running)

        assertEquals(1, flow.first().size)
    }

    @Test
    fun summaryTypeMatchesWorkoutType() = runTest {
        repository.createWorkout(WorkoutType.Strength)

        val summary = useCase().first().first()

        assertEquals(WorkoutType.Strength, summary.type)
    }
}
