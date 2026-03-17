package com.jepittman.tempo.core.domain.usecase

import com.jepittman.tempo.core.domain.model.ActiveWorkout
import com.jepittman.tempo.core.domain.model.WorkoutStatus
import com.jepittman.tempo.core.domain.repository.HeartRateRepository
import com.jepittman.tempo.core.domain.repository.WorkoutRepository
import kotlinx.datetime.Clock

class FinishWorkoutUseCase(
    private val workoutRepository: WorkoutRepository,
    private val heartRateRepository: HeartRateRepository,
) {
    suspend operator fun invoke(activeWorkout: ActiveWorkout): ActiveWorkout {
        val finished = activeWorkout.workout.copy(endedAt = Clock.System.now())
        workoutRepository.saveWorkout(finished)
        return activeWorkout.copy(
            workout = finished,
            status = WorkoutStatus.Completed,
        )
    }
}
