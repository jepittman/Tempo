package com.jepittman.tempo.core.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jepittman.tempo.core.domain.model.WorkoutSet
import com.jepittman.tempo.core.domain.repository.WorkoutSetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SqlDelightWorkoutSetRepository(
    private val database: TempoDatabase,
) : WorkoutSetRepository {

    private val queries = database.workoutSetEntityQueries

    override fun getSets(workoutId: String): Flow<List<WorkoutSet>> =
        queries.selectByWorkoutId(workoutId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun saveSet(set: WorkoutSet): WorkoutSet {
        val id = set.id.ifBlank { Uuid.random().toString() }
        val persisted = set.copy(id = id)
        queries.insert(
            id = persisted.id,
            workout_id = persisted.workoutId,
            exercise_name = persisted.exerciseName,
            set_number = persisted.setNumber.toLong(),
            reps = persisted.reps?.toLong(),
            weight_kg = persisted.weightKg,
            duration_seconds = persisted.durationSeconds,
            completed_at = persisted.completedAt,
        )
        return persisted
    }

    override suspend fun deleteSet(id: String) {
        queries.deleteById(id)
    }

    private fun WorkoutSetEntity.toDomain() = WorkoutSet(
        id = id,
        workoutId = workout_id,
        exerciseName = exercise_name,
        setNumber = set_number.toInt(),
        reps = reps?.toInt(),
        weightKg = weight_kg,
        durationSeconds = duration_seconds,
        completedAt = completed_at,
    )
}
