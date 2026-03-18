package com.jepittman.tempo.core.domain.fake

import com.jepittman.tempo.core.domain.model.Workout
import com.jepittman.tempo.core.domain.model.WorkoutSummary
import com.jepittman.tempo.core.domain.model.WorkoutType
import com.jepittman.tempo.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class FakeWorkoutRepository : WorkoutRepository {

    private val store = MutableStateFlow<Map<String, Workout>>(emptyMap())

    /** Summaries explicitly saved via [saveSummary] — inspectable in tests. */
    val savedSummaries = mutableListOf<WorkoutSummary>()

    override fun getWorkoutSummaries(): Flow<List<WorkoutSummary>> =
        store.map { workouts -> workouts.values.map { it.toSummary() } }

    override suspend fun getWorkout(id: String): Workout? = store.value[id]

    override suspend fun createWorkout(type: WorkoutType, title: String?): Workout {
        val workout = Workout(
            id = "workout-${store.value.size + 1}",
            type = type,
            title = title,
            startedAt = Clock.System.now(),
            endedAt = null,
            notes = null,
        )
        store.value = store.value + (workout.id to workout)
        return workout
    }

    override suspend fun saveWorkout(workout: Workout): Workout {
        store.value = store.value + (workout.id to workout)
        return workout
    }

    override suspend fun saveSummary(summary: WorkoutSummary) {
        savedSummaries += summary
    }

    override suspend fun deleteWorkout(id: String) {
        store.value = store.value - id
    }

    private fun Workout.toSummary() = WorkoutSummary(
        workoutId = id,
        type = type,
        title = title,
        startedAt = startedAt,
        durationSeconds = endedAt?.let { (it - startedAt).inWholeSeconds } ?: 0L,
        averageHeartRateBpm = null,
        maxHeartRateBpm = null,
        totalSets = null,
        totalVolumeKg = null,
    )
}
