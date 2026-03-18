package com.jepittman.tempo.core.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.jepittman.tempo.core.domain.model.DataSource
import com.jepittman.tempo.core.domain.model.HeartRateSample
import com.jepittman.tempo.core.domain.model.WorkoutType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SqlDelightHeartRateRepositoryTest {

    private lateinit var heartRateRepository: SqlDelightHeartRateRepository
    private lateinit var workoutRepository: SqlDelightWorkoutRepository

    @BeforeTest
    fun setUp() {
        val database = DatabaseFactory(JvmDriverFactory(JdbcSqliteDriver.IN_MEMORY)).create()
        heartRateRepository = SqlDelightHeartRateRepository(database)
        workoutRepository = SqlDelightWorkoutRepository(database)
    }

    @Test
    fun saveSampleAndRetrieveByWorkoutId() = runTest {
        val workout = workoutRepository.createWorkout(WorkoutType.Running)
        val sample = HeartRateSample(
            workoutId = workout.id,
            bpm = 145,
            recordedAt = Clock.System.now(),
            source = DataSource.WearOs,
        )

        heartRateRepository.saveSample(sample)

        val samples = heartRateRepository.getSamples(workout.id).first()
        assertEquals(1, samples.size)
        assertEquals(145, samples.first().bpm)
        assertEquals(DataSource.WearOs, samples.first().source)
    }

    @Test
    fun saveSamplesInsertsAll() = runTest {
        val workout = workoutRepository.createWorkout(WorkoutType.Cycling)
        val now = Clock.System.now()
        val samples = listOf(
            HeartRateSample(workout.id, 120, now, DataSource.WearOs),
            HeartRateSample(workout.id, 130, now, DataSource.WearOs),
            HeartRateSample(workout.id, 140, now, DataSource.WearOs),
        )

        heartRateRepository.saveSamples(samples)

        assertEquals(3, heartRateRepository.getSamples(workout.id).first().size)
    }

    @Test
    fun getSamplesOnlyReturnsMatchingWorkoutId() = runTest {
        val workoutA = workoutRepository.createWorkout(WorkoutType.Running)
        val workoutB = workoutRepository.createWorkout(WorkoutType.Cycling)
        val now = Clock.System.now()

        heartRateRepository.saveSample(HeartRateSample(workoutA.id, 150, now, DataSource.WatchOs))
        heartRateRepository.saveSample(HeartRateSample(workoutB.id, 160, now, DataSource.WatchOs))

        val samplesA = heartRateRepository.getSamples(workoutA.id).first()
        assertEquals(1, samplesA.size)
        assertEquals(150, samplesA.first().bpm)
    }

    @Test
    fun emptyFlowForWorkoutWithNoSamples() = runTest {
        val workout = workoutRepository.createWorkout(WorkoutType.Strength)

        val samples = heartRateRepository.getSamples(workout.id).first()

        assertTrue(samples.isEmpty())
    }
}
