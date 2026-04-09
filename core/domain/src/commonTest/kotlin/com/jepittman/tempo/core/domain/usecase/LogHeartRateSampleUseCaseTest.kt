package com.jepittman.tempo.core.domain.usecase

import com.jepittman.tempo.core.domain.fake.FakeHeartRateRepository
import com.jepittman.tempo.core.domain.model.DataSource
import com.jepittman.tempo.core.domain.model.HeartRateSample
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals

class LogHeartRateSampleUseCaseTest {

    private val repository = FakeHeartRateRepository()
    private val useCase = LogHeartRateSampleUseCase(repository)

    @Test
    fun sampleIsPersistedInRepository() = runTest {
        val sample = HeartRateSample(
            workoutId = "workout-1",
            bpm = 145,
            recordedAt = Clock.System.now(),
            source = DataSource.WearOs,
        )

        useCase(sample)

        val stored = repository.getSamples("workout-1").first()
        assertEquals(1, stored.size)
        assertEquals(145, stored.first().bpm)
    }

    @Test
    fun samplesFromDifferentWorkoutsAreIsolated() = runTest {
        useCase(HeartRateSample("workout-1", 140, Clock.System.now(), DataSource.WearOs))
        useCase(HeartRateSample("workout-2", 160, Clock.System.now(), DataSource.WatchOs))

        assertEquals(1, repository.getSamples("workout-1").first().size)
        assertEquals(1, repository.getSamples("workout-2").first().size)
    }

    @Test
    fun sourceIsPreserved() = runTest {
        val sample = HeartRateSample("workout-1", 130, Clock.System.now(), DataSource.WatchOs)

        useCase(sample)

        val stored = repository.getSamples("workout-1").first()
        assertEquals(DataSource.WatchOs, stored.first().source)
    }
}
