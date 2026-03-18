package com.jepittman.tempo.core.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jepittman.tempo.core.domain.model.Workout
import com.jepittman.tempo.core.domain.model.WorkoutSummary
import com.jepittman.tempo.core.domain.model.WorkoutType
import com.jepittman.tempo.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SqlDelightWorkoutRepository(
    private val database: TempoDatabase,
) : WorkoutRepository {

    private val queries = database.workoutEntityQueries
    private val summaryQueries = database.workoutSummaryEntityQueries

    override fun getWorkoutSummaries(): Flow<List<WorkoutSummary>> =
        summaryQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun getWorkout(id: String): Workout? =
        queries.selectById(id).executeAsOneOrNull()?.toDomain()

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createWorkout(type: WorkoutType, title: String?): Workout {
        val workout = Workout(
            id = Uuid.random().toString(),
            type = type,
            title = title,
            startedAt = Clock.System.now(),
            endedAt = null,
            notes = null,
        )
        queries.insert(
            id = workout.id,
            type = workout.type,
            title = workout.title,
            started_at = workout.startedAt,
            ended_at = workout.endedAt,
            notes = workout.notes,
        )
        return workout
    }

    override suspend fun saveWorkout(workout: Workout): Workout {
        queries.insert(
            id = workout.id,
            type = workout.type,
            title = workout.title,
            started_at = workout.startedAt,
            ended_at = workout.endedAt,
            notes = workout.notes,
        )
        return workout
    }

    override suspend fun saveSummary(summary: WorkoutSummary) {
        summaryQueries.upsert(
            workout_id = summary.workoutId,
            type = summary.type,
            title = summary.title,
            started_at = summary.startedAt,
            duration_seconds = summary.durationSeconds,
            average_heart_rate_bpm = summary.averageHeartRateBpm?.toLong(),
            max_heart_rate_bpm = summary.maxHeartRateBpm?.toLong(),
            total_sets = summary.totalSets?.toLong(),
            total_volume_kg = summary.totalVolumeKg,
        )
    }

    override suspend fun deleteWorkout(id: String) {
        queries.deleteById(id)
    }

    private fun WorkoutEntity.toDomain() = Workout(
        id = id,
        type = type,
        title = title,
        startedAt = started_at,
        endedAt = ended_at,
        notes = notes,
    )

    private fun WorkoutSummaryEntity.toDomain() = WorkoutSummary(
        workoutId = workout_id,
        type = type,
        title = title,
        startedAt = started_at,
        durationSeconds = duration_seconds,
        averageHeartRateBpm = average_heart_rate_bpm?.toInt(),
        maxHeartRateBpm = max_heart_rate_bpm?.toInt(),
        totalSets = total_sets?.toInt(),
        totalVolumeKg = total_volume_kg,
    )
}
