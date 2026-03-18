package com.jepittman.tempo.core.domain.usecase

import com.jepittman.tempo.core.domain.model.ActiveWorkout
import com.jepittman.tempo.core.domain.model.WorkoutStatus
import com.jepittman.tempo.core.domain.model.WorkoutType
import com.jepittman.tempo.core.domain.repository.WorkoutRepository

class StartWorkoutUseCase(private val repository: WorkoutRepository) {
    suspend operator fun invoke(type: WorkoutType, title: String? = null): ActiveWorkout {
        val workout = repository.createWorkout(type, title)
        return ActiveWorkout(
            workout = workout,
            status = WorkoutStatus.Preparing,
            elapsedSeconds = 0L,
            currentHeartRateBpm = null,
            sets = emptyList(),
        )
    }
}
