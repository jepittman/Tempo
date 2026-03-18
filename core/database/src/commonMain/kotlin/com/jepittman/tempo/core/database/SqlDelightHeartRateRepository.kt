package com.jepittman.tempo.core.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jepittman.tempo.core.domain.model.HeartRateSample
import com.jepittman.tempo.core.domain.repository.HeartRateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SqlDelightHeartRateRepository(
    private val database: TempoDatabase,
) : HeartRateRepository {

    private val queries = database.heartRateSampleEntityQueries

    override fun getSamples(workoutId: String): Flow<List<HeartRateSample>> =
        queries.selectByWorkoutId(workoutId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun saveSample(sample: HeartRateSample) {
        queries.insert(
            workout_id = sample.workoutId,
            bpm = sample.bpm.toLong(),
            recorded_at = sample.recordedAt,
            source = sample.source,
        )
    }

    override suspend fun saveSamples(samples: List<HeartRateSample>) {
        database.transaction {
            samples.forEach { sample ->
                queries.insert(
                    workout_id = sample.workoutId,
                    bpm = sample.bpm.toLong(),
                    recorded_at = sample.recordedAt,
                    source = sample.source,
                )
            }
        }
    }

    private fun HeartRateSampleEntity.toDomain() = HeartRateSample(
        workoutId = workout_id,
        bpm = bpm.toInt(),
        recordedAt = recorded_at,
        source = source,
    )
}
