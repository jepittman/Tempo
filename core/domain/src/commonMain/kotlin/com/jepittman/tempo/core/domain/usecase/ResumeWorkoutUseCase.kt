package com.jepittman.tempo.core.domain.usecase

import com.jepittman.tempo.core.domain.model.ActiveWorkout
import com.jepittman.tempo.core.domain.model.WorkoutStatus

class ResumeWorkoutUseCase {
    operator fun invoke(activeWorkout: ActiveWorkout): ActiveWorkout {
        require(activeWorkout.status == WorkoutStatus.Paused) {
            "Cannot resume a workout with status ${activeWorkout.status}"
        }
        return activeWorkout.copy(status = WorkoutStatus.Active)
    }
}
