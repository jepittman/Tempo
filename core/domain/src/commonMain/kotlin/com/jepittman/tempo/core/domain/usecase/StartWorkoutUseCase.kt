package com.jepittman.tempo.core.domain.usecase

import com.jepittman.tempo.core.domain.model.ActiveWorkout
import com.jepittman.tempo.core.domain.model.WorkoutStatus
import com.jepittman.tempo.core.domain.model.WorkoutType
import com.jepittman.tempo.core.domain.repository.WorkoutRepository

class StartWorkoutUseCase(private val workoutRepository: WorkoutRepository) {
    suspend operator fun invoke(type: WorkoutType, title: String? = null): ActiveWorkout {
        val workout = workoutRepository.createWorkout(type, title)
        return ActiveWorkout(
            workout = workout,
            status = WorkoutStatus.Active,
            elapsedSeconds = 0,
            currentHeartRateBpm = null,
            sets = emptyList(),
        )
    }
}
