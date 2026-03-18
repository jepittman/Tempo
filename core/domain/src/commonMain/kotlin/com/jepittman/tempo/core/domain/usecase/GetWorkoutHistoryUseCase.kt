package com.jepittman.tempo.core.domain.usecase

import com.jepittman.tempo.core.domain.model.WorkoutSummary
import com.jepittman.tempo.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow

class GetWorkoutHistoryUseCase(private val repository: WorkoutRepository) {
    operator fun invoke(): Flow<List<WorkoutSummary>> = repository.getWorkoutSummaries()
}
