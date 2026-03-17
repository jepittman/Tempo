package com.jepittman.tempo.core.domain.model

import kotlinx.datetime.Instant

data class HeartRateSample(
    val workoutId: String,
    val bpm: Int,
    val recordedAt: Instant,
    val source: DataSource,
)

enum class DataSource {
    WearOs,
    WatchOs,
    Manual,
}
