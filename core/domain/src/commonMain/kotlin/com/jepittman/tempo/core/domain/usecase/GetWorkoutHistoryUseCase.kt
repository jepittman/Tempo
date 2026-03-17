package com.jepittman.tempo.core.domain.usecase

import com.jepittman.tempo.core.domain.model.WorkoutSummary
import com.jepittman.tempo.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow

class GetWorkoutHistoryUseCase(private val workoutRepository: WorkoutRepository) {
    operator fun invoke(): Flow<List<WorkoutSummary>> = workoutRepository.getWorkoutSummaries()
}
