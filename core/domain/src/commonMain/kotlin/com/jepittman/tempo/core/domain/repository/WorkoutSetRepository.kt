package com.jepittman.tempo.core.domain.repository

import com.jepittman.tempo.core.domain.model.WorkoutSet
import kotlinx.coroutines.flow.Flow

interface WorkoutSetRepository {
    fun getSets(workoutId: String): Flow<List<WorkoutSet>>
    suspend fun saveSet(set: WorkoutSet): WorkoutSet
    suspend fun deleteSet(id: String)
}
