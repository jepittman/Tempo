package com.jepittman.tempo.core.domain.model

import kotlinx.datetime.Instant

data class WorkoutSet(
    val id: String,
    val workoutId: String,
    val exerciseName: String,
    val setNumber: Int,
    val reps: Int?,
    val weightKg: Double?,
    val durationSeconds: Long?,
    val completedAt: Instant,
)
