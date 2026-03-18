package com.jepittman.tempo.core.database

import com.jepittman.tempo.core.domain.model.WorkoutSummary
import com.jepittman.tempo.core.domain.model.WorkoutType
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal fun buildSummary(
    workoutId: String = "workout-1",
    type: WorkoutType = WorkoutType.Strength,
    title: String? = null,
    startedAt: Instant = Clock.System.now(),
    durationSeconds: Long = 3600L,
    averageHeartRateBpm: Int? = null,
    maxHeartRateBpm: Int? = null,
    totalSets: Int? = null,
    totalVolumeKg: Double? = null,
) = WorkoutSummary(
    workoutId = workoutId,
    type = type,
    title = title,
    startedAt = startedAt,
    durationSeconds = durationSeconds,
    averageHeartRateBpm = averageHeartRateBpm,
    maxHeartRateBpm = maxHeartRateBpm,
    totalSets = totalSets,
    totalVolumeKg = totalVolumeKg,
)
