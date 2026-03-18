package com.jepittman.tempo.core.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.jepittman.tempo.core.domain.model.WorkoutType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.BeforeTest
import kotlin.time.Duration.Companion.seconds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SqlDelightWorkoutRepositoryTest {

    private lateinit var repository: SqlDelightWorkoutRepository

    @BeforeTest
    fun setUp() {
        val database = DatabaseFactory(JvmDriverFactory(JdbcSqliteDriver.IN_MEMORY)).create()
        repository = SqlDelightWorkoutRepository(database)
    }

    @Test
    fun createWorkoutPersistsAndReturnsWorkout() = runTest {
        val workout = repository.createWorkout(WorkoutType.Strength, "Morning lift")

        assertNotNull(workout.id)
        assertEquals(WorkoutType.Strength, workout.type)
        assertEquals("Morning lift", workout.title)
        assertNull(workout.endedAt)
    }

    @Test
    fun getWorkoutReturnsNullForUnknownId() = runTest {
        assertNull(repository.getWorkout("does-not-exist"))
    }

    @Test
    fun getWorkoutReturnsPersistedWorkout() = runTest {
        val workout = repository.createWorkout(WorkoutType.Running)

        val fetched = repository.getWorkout(workout.id)

        assertNotNull(fetched)
        assertEquals(workout.id, fetched.id)
        assertEquals(WorkoutType.Running, fetched.type)
    }

    @Test
    fun saveWorkoutUpdatesExistingRecord() = runTest {
        val workout = repository.createWorkout(WorkoutType.Cycling)
        val updated = workout.copy(title = "Evening ride")

        repository.saveWorkout(updated)

        assertEquals("Evening ride", repository.getWorkout(workout.id)?.title)
    }

    @Test
    fun deleteWorkoutRemovesRecord() = runTest {
        val workout = repository.createWorkout(WorkoutType.Yoga)

        repository.deleteWorkout(workout.id)

        assertNull(repository.getWorkout(workout.id))
    }

    @Test
    fun saveSummaryAppearsInSummariesFlow() = runTest {
        val workout = repository.createWorkout(WorkoutType.Hiit)
        val summary = buildSummary(workoutId = workout.id, type = WorkoutType.Hiit)

        repository.saveSummary(summary)

        val summaries = repository.getWorkoutSummaries().first()
        assertEquals(1, summaries.size)
        assertEquals(workout.id, summaries.first().workoutId)
    }

    @Test
    fun summariesAreOrderedByStartedAtDescending() = runTest {
        val base = Clock.System.now()
        val first = repository.createWorkout(WorkoutType.Running)
        val second = repository.createWorkout(WorkoutType.Cycling)

        repository.saveSummary(
            buildSummary(workoutId = first.id, type = WorkoutType.Running, startedAt = base),
        )
        repository.saveSummary(
            buildSummary(
                workoutId = second.id,
                type = WorkoutType.Cycling,
                startedAt = base + 1.seconds,
            ),
        )

        val summaries = repository.getWorkoutSummaries().first()
        assertEquals(second.id, summaries.first().workoutId)
    }
}
