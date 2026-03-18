package com.jepittman.tempo.core.domain.usecase

import com.jepittman.tempo.core.domain.model.ActiveWorkout
import com.jepittman.tempo.core.domain.model.WorkoutStatus
import com.jepittman.tempo.core.domain.repository.WorkoutRepository

class DiscardWorkoutUseCase(private val repository: WorkoutRepository) {
    suspend operator fun invoke(activeWorkout: ActiveWorkout): ActiveWorkout {
        repository.deleteWorkout(activeWorkout.workout.id)
        return activeWorkout.copy(status = WorkoutStatus.Discarded)
    }
}
