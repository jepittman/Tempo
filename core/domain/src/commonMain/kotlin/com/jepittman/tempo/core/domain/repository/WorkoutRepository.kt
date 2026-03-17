package com.jepittman.tempo.core.domain.repository

import com.jepittman.tempo.core.domain.model.Workout
import com.jepittman.tempo.core.domain.model.WorkoutSummary
import com.jepittman.tempo.core.domain.model.WorkoutType
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun getWorkoutSummaries(): Flow<List<WorkoutSummary>>
    suspend fun getWorkout(id: String): Workout?
    suspend fun createWorkout(type: WorkoutType, title: String? = null): Workout
    suspend fun saveWorkout(workout: Workout): Workout
    suspend fun deleteWorkout(id: String)
}
