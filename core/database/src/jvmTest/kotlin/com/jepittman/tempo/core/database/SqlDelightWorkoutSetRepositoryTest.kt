package com.jepittman.tempo.core.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.jepittman.tempo.core.domain.model.WorkoutType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SqlDelightWorkoutSetRepositoryTest {

    private lateinit var setRepository: SqlDelightWorkoutSetRepository
    private lateinit var workoutRepository: SqlDelightWorkoutRepository

    @BeforeTest
    fun setUp() {
        val database = DatabaseFactory(JvmDriverFactory(JdbcSqliteDriver.IN_MEMORY)).create()
        setRepository = SqlDelightWorkoutSetRepository(database)
        workoutRepository = SqlDelightWorkoutRepository(database)
    }

    @Test
    fun saveSetGeneratesIdWhenBlank() = runTest {
        val workout = workoutRepository.createWorkout(WorkoutType.Strength)

        val saved = setRepository.saveSet(buildWorkoutSet(workoutId = workout.id))

        assertNotNull(saved.id)
        assertTrue(saved.id.isNotBlank())
    }

    @Test
    fun saveSetPreservesExplicitId() = runTest {
        val workout = workoutRepository.createWorkout(WorkoutType.Strength)

        val saved = setRepository.saveSet(
            buildWorkoutSet(id = "my-id", workoutId = workout.id),
        )

        assertEquals("my-id", saved.id)
    }

    @Test
    fun saveSetRoundTripsAllFields() = runTest {
        val workout = workoutRepository.createWorkout(WorkoutType.Strength)
        val set = buildWorkoutSet(
            workoutId = workout.id,
            exerciseName = "Squat",
            setNumber = 3,
            reps = 5,
            weightKg = 120.0,
            durationSeconds = null,
        )

        val saved = setRepository.saveSet(set)
        val fetched = setRepository.getSets(workout.id).first().first()

        assertEquals(saved.id, fetched.id)
        assertEquals("Squat", fetched.exerciseName)
        assertEquals(3, fetched.setNumber)
        assertEquals(5, fetched.reps)
        assertEquals(120.0, fetched.weightKg)
        assertNull(fetched.durationSeconds)
    }

    @Test
    fun saveSetRoundTripsNullableFields() = runTest {
        val workout = workoutRepository.createWorkout(WorkoutType.Hiit)
        val set = buildWorkoutSet(
            workoutId = workout.id,
            reps = null,
            weightKg = null,
            durationSeconds = 45L,
        )

        setRepository.saveSet(set)
        val fetched = setRepository.getSets(workout.id).first().first()

        assertNull(fetched.reps)
        assertNull(fetched.weightKg)
        assertEquals(45L, fetched.durationSeconds)
    }

    @Test
    fun saveSetUpsertOverwritesExistingRecord() = runTest {
        val workout = workoutRepository.createWorkout(WorkoutType.Strength)
        val saved = setRepository.saveSet(
            buildWorkoutSet(workoutId = workout.id, weightKg = 60.0),
        )

        setRepository.saveSet(saved.copy(weightKg = 65.0))

        val sets = setRepository.getSets(workout.id).first()
        assertEquals(1, sets.size)
        assertEquals(65.0, sets.first().weightKg)
    }

    @Test
    fun getSetsReturnsEmptyForWorkoutWithNoSets() = runTest {
        val workout = workoutRepository.createWorkout(WorkoutType.Yoga)

        val sets = setRepository.getSets(workout.id).first()

        assertTrue(sets.isEmpty())
    }

    @Test
    fun getSetsOrdersBySetNumber() = runTest {
        val workout = workoutRepository.createWorkout(WorkoutType.Strength)

        setRepository.saveSet(buildWorkoutSet(workoutId = workout.id, setNumber = 3))
        setRepository.saveSet(buildWorkoutSet(workoutId = workout.id, setNumber = 1))
        setRepository.saveSet(buildWorkoutSet(workoutId = workout.id, setNumber = 2))

        val sets = setRepository.getSets(workout.id).first()
        assertEquals(listOf(1, 2, 3), sets.map { it.setNumber })
    }

    @Test
    fun getSetsFiltersToMatchingWorkout() = runTest {
        val workoutA = workoutRepository.createWorkout(WorkoutType.Strength)
        val workoutB = workoutRepository.createWorkout(WorkoutType.Cycling)

        setRepository.saveSet(buildWorkoutSet(workoutId = workoutA.id, exerciseName = "Deadlift"))
        setRepository.saveSet(buildWorkoutSet(workoutId = workoutB.id, exerciseName = "Pedal"))

        val setsA = setRepository.getSets(workoutA.id).first()
        assertEquals(1, setsA.size)
        assertEquals("Deadlift", setsA.first().exerciseName)
    }

    @Test
    fun deleteSetRemovesRecord() = runTest {
        val workout = workoutRepository.createWorkout(WorkoutType.Strength)
        val saved = setRepository.saveSet(buildWorkoutSet(workoutId = workout.id))

        setRepository.deleteSet(saved.id)

        assertTrue(setRepository.getSets(workout.id).first().isEmpty())
    }

    @Test
    fun deleteSetIsNoOpForUnknownId() = runTest {
        val workout = workoutRepository.createWorkout(WorkoutType.Strength)
        setRepository.saveSet(buildWorkoutSet(workoutId = workout.id))

        setRepository.deleteSet("does-not-exist")

        assertEquals(1, setRepository.getSets(workout.id).first().size)
    }

    @Test
    fun deleteSetOnlyRemovesTargetedSet() = runTest {
        val workout = workoutRepository.createWorkout(WorkoutType.Strength)
        val setA = setRepository.saveSet(buildWorkoutSet(workoutId = workout.id, setNumber = 1))
        setRepository.saveSet(buildWorkoutSet(workoutId = workout.id, setNumber = 2))

        setRepository.deleteSet(setA.id)

        val remaining = setRepository.getSets(workout.id).first()
        assertEquals(1, remaining.size)
        assertEquals(2, remaining.first().setNumber)
    }
}
