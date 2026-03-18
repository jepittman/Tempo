package com.jepittman.tempo.core.domain.model

/**
 * Aggregate representing a workout currently in progress.
 *
 * Intentionally kept as a value type — state is advanced by use cases and collected
 * by the feature layer via a StateFlow, not mutated in place.
 */
data class ActiveWorkout(
    val workout: Workout,
    val status: WorkoutStatus,
    val elapsedSeconds: Long,
    val currentHeartRateBpm: Int?,
    val sets: List<WorkoutSet>,
)
