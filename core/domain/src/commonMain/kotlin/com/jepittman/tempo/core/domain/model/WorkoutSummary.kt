package com.jepittman.tempo.core.domain.model

import kotlin.time.Instant

data class WorkoutSummary(
    val workoutId: String,
    val type: WorkoutType,
    val title: String?,
    val startedAt: Instant,
    val durationSeconds: Long,
    val averageHeartRateBpm: Int?,
    val maxHeartRateBpm: Int?,
    val totalSets: Int?,
    val totalVolumeKg: Double?,
)
