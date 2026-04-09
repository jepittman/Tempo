package com.jepittman.tempo.core.domain.model

import kotlin.time.Instant

data class Workout(
    val id: String,
    val type: WorkoutType,
    val title: String?,
    val startedAt: Instant,
    val endedAt: Instant?,
    val notes: String?,
)
