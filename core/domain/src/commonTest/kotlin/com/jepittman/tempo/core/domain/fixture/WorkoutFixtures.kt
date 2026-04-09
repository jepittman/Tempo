package com.jepittman.tempo.core.domain.fixture

import com.jepittman.tempo.core.domain.model.ActiveWorkout
import com.jepittman.tempo.core.domain.model.Workout
import com.jepittman.tempo.core.domain.model.WorkoutSet
import com.jepittman.tempo.core.domain.model.WorkoutStatus
import com.jepittman.tempo.core.domain.model.WorkoutType
import kotlin.time.Instant

fun buildWorkout(
    id: String = "workout-1",
    type: WorkoutType = WorkoutType.Running,
    title: String? = null,
    startedAt: Instant = Instant.fromEpochSeconds(0),
    endedAt: Instant? = null,
    notes: String? = null,
) = Workout(
    id = id,
    type = type,
    title = title,
    startedAt = startedAt,
    endedAt = endedAt,
    notes = notes,
)

fun buildActiveWorkout(
    workout: Workout = buildWorkout(),
    status: WorkoutStatus = WorkoutStatus.Active,
    elapsedSeconds: Long = 0L,
    currentHeartRateBpm: Int? = null,
    sets: List<WorkoutSet> = emptyList(),
) = ActiveWorkout(
    workout = workout,
    status = status,
    elapsedSeconds = elapsedSeconds,
    currentHeartRateBpm = currentHeartRateBpm,
    sets = sets,
)
