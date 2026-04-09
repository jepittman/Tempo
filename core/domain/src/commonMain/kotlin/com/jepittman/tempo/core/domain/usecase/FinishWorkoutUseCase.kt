package com.jepittman.tempo.core.domain.usecase

import com.jepittman.tempo.core.domain.model.ActiveWorkout
import com.jepittman.tempo.core.domain.model.WorkoutStatus
import com.jepittman.tempo.core.domain.model.WorkoutSummary
import com.jepittman.tempo.core.domain.repository.HeartRateRepository
import com.jepittman.tempo.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import kotlin.time.Clock

class FinishWorkoutUseCase(
    private val workoutRepository: WorkoutRepository,
    private val heartRateRepository: HeartRateRepository,
) {
    suspend operator fun invoke(activeWorkout: ActiveWorkout): ActiveWorkout {
        require(activeWorkout.status == WorkoutStatus.Active || activeWorkout.status == WorkoutStatus.Paused) {
            "Cannot finish a workout with status ${activeWorkout.status}"
        }

        val now = Clock.System.now()
        val finished = activeWorkout.workout.copy(endedAt = now)
        workoutRepository.saveWorkout(finished)

        val hrSamples = heartRateRepository.getSamples(finished.id).first()
        val summary = WorkoutSummary(
            workoutId = finished.id,
            type = finished.type,
            title = finished.title,
            startedAt = finished.startedAt,
            durationSeconds = (now - finished.startedAt).inWholeSeconds,
            averageHeartRateBpm = hrSamples.takeIf { it.isNotEmpty() }
                ?.let { samples -> samples.sumOf { it.bpm } / samples.size },
            maxHeartRateBpm = hrSamples.maxOfOrNull { it.bpm },
            totalSets = activeWorkout.sets.size.takeIf { it > 0 },
            totalVolumeKg = activeWorkout.sets
                .mapNotNull { set -> set.weightKg?.let { w -> set.reps?.let { r -> w * r } } }
                .takeIf { it.isNotEmpty() }
                ?.sum(),
        )
        workoutRepository.saveSummary(summary)

        return activeWorkout.copy(
            workout = finished,
            status = WorkoutStatus.Completed,
        )
    }
}
