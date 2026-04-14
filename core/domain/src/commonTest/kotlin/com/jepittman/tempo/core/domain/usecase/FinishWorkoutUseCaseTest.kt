package com.jepittman.tempo.core.domain.usecase

import com.jepittman.tempo.core.domain.fake.FakeHeartRateRepository
import com.jepittman.tempo.core.domain.fake.FakeWorkoutRepository
import com.jepittman.tempo.core.domain.fixture.buildActiveWorkout
import com.jepittman.tempo.core.domain.fixture.buildWorkoutSet
import com.jepittman.tempo.core.domain.model.DataSource
import com.jepittman.tempo.core.domain.model.HeartRateSample
import com.jepittman.tempo.core.domain.model.WorkoutStatus
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FinishWorkoutUseCaseTest {

    private val workoutRepository = FakeWorkoutRepository()
    private val heartRateRepository = FakeHeartRateRepository()
    private val useCase = FinishWorkoutUseCase(workoutRepository, heartRateRepository)

    @Test
    fun setsStatusToCompleted() = runTest {
        val active = buildActiveWorkout(status = WorkoutStatus.Active)
        workoutRepository.saveWorkout(active.workout)

        val result = useCase(active)

        assertEquals(WorkoutStatus.Completed, result.status)
    }

    @Test
    fun stampsEndedAt() = runTest {
        val active = buildActiveWorkout()
        workoutRepository.saveWorkout(active.workout)

        val result = useCase(active)

        assertNotNull(result.workout.endedAt)
    }

    @Test
    fun persistsFinishedWorkout() = runTest {
        val active = buildActiveWorkout()
        workoutRepository.saveWorkout(active.workout)

        val result = useCase(active)

        assertNotNull(workoutRepository.getWorkout(result.workout.id)?.endedAt)
    }

    @Test
    fun savesSummaryOnCompletion() = runTest {
        val active = buildActiveWorkout()
        workoutRepository.saveWorkout(active.workout)

        useCase(active)

        assertEquals(1, workoutRepository.savedSummaries.size)
        assertEquals(active.workout.id, workoutRepository.savedSummaries.first().workoutId)
    }

    @Test
    fun summaryIncludesAverageAndMaxHeartRate() = runTest {
        val active = buildActiveWorkout()
        workoutRepository.saveWorkout(active.workout)
        val now = Clock.System.now()
        heartRateRepository.saveSamples(
            listOf(
                HeartRateSample(active.workout.id, 120, now, DataSource.WearOs),
                HeartRateSample(active.workout.id, 140, now, DataSource.WearOs),
                HeartRateSample(active.workout.id, 160, now, DataSource.WearOs),
            ),
        )

        useCase(active)

        val summary = workoutRepository.savedSummaries.first()
        assertEquals(140, summary.averageHeartRateBpm)
        assertEquals(160, summary.maxHeartRateBpm)
    }

    @Test
    fun summaryHasNullHeartRateWhenNoSamplesExist() = runTest {
        val active = buildActiveWorkout()
        workoutRepository.saveWorkout(active.workout)

        useCase(active)

        val summary = workoutRepository.savedSummaries.first()
        assertTrue(summary.averageHeartRateBpm == null)
        assertTrue(summary.maxHeartRateBpm == null)
    }

    @Test
    fun canFinishAPausedWorkout() = runTest {
        val paused = buildActiveWorkout(status = WorkoutStatus.Paused)
        workoutRepository.saveWorkout(paused.workout)

        val result = useCase(paused)

        assertEquals(WorkoutStatus.Completed, result.status)
    }

    @Test
    fun throwsWhenWorkoutIsAlreadyCompleted() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase(buildActiveWorkout(status = WorkoutStatus.Completed))
        }
    }

    @Test
    fun throwsWhenWorkoutIsDiscarded() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase(buildActiveWorkout(status = WorkoutStatus.Discarded))
        }
    }

    @Test
    fun summaryComputesTotalVolumeFromSetsWithWeightAndReps() = runTest {
        val active = buildActiveWorkout(
            sets = listOf(
                buildWorkoutSet(setNumber = 1, reps = 10, weightKg = 80.0),  // 800 kg
                buildWorkoutSet(setNumber = 2, reps = 8, weightKg = 85.0),   // 680 kg
            ),
        )
        workoutRepository.saveWorkout(active.workout)

        useCase(active)

        val summary = workoutRepository.savedSummaries.first()
        assertEquals(1480.0, summary.totalVolumeKg)
    }

    @Test
    fun summaryExcludesSetsWithNullWeightFromVolume() = runTest {
        val active = buildActiveWorkout(
            sets = listOf(
                buildWorkoutSet(setNumber = 1, reps = 10, weightKg = null),
                buildWorkoutSet(setNumber = 2, reps = 8, weightKg = 80.0),  // 640 kg
            ),
        )
        workoutRepository.saveWorkout(active.workout)

        useCase(active)

        val summary = workoutRepository.savedSummaries.first()
        assertEquals(640.0, summary.totalVolumeKg)
    }

    @Test
    fun summaryExcludesSetsWithNullRepsFromVolume() = runTest {
        val active = buildActiveWorkout(
            sets = listOf(
                buildWorkoutSet(setNumber = 1, reps = null, weightKg = 80.0),
                buildWorkoutSet(setNumber = 2, reps = 10, weightKg = 60.0),  // 600 kg
            ),
        )
        workoutRepository.saveWorkout(active.workout)

        useCase(active)

        val summary = workoutRepository.savedSummaries.first()
        assertEquals(600.0, summary.totalVolumeKg)
    }

    @Test
    fun summaryHasNullVolumeWhenNoSetsHaveWeightAndReps() = runTest {
        val active = buildActiveWorkout(
            sets = listOf(
                buildWorkoutSet(setNumber = 1, reps = null, weightKg = null),
            ),
        )
        workoutRepository.saveWorkout(active.workout)

        useCase(active)

        val summary = workoutRepository.savedSummaries.first()
        assertEquals(null, summary.totalVolumeKg)
    }
}
