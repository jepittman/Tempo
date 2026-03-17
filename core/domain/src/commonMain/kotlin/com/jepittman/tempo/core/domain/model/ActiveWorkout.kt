package com.jepittman.tempo.core.domain.model

data class ActiveWorkout(
    val workout: Workout,
    val status: WorkoutStatus,
    val elapsedSeconds: Long,
    val currentHeartRateBpm: Int?,
    val sets: List<WorkoutSet>,
)

enum class WorkoutStatus {
    Preparing,
    Active,
    Paused,
    Completed,
}
