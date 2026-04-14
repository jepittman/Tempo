package com.jepittman.tempo.core.database

import com.jepittman.tempo.core.domain.model.WorkoutSet
import com.jepittman.tempo.core.domain.model.WorkoutSummary
import com.jepittman.tempo.core.domain.model.WorkoutType
import kotlin.time.Clock
import kotlin.time.Instant

internal fun buildWorkoutSet(
    id: String = "",
    workoutId: String = "workout-1",
    exerciseName: String = "Bench Press",
    setNumber: Int = 1,
    reps: Int? = 10,
    weightKg: Double? = 80.0,
    durationSeconds: Long? = null,
    completedAt: Instant = Clock.System.now(),
) = WorkoutSet(
    id = id,
    workoutId = workoutId,
    exerciseName = exerciseName,
    setNumber = setNumber,
    reps = reps,
    weightKg = weightKg,
    durationSeconds = durationSeconds,
    completedAt = completedAt,
)

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
