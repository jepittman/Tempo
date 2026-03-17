package com.jepittman.tempo.core.domain.repository

import com.jepittman.tempo.core.domain.model.HeartRateSample
import kotlinx.coroutines.flow.Flow

interface HeartRateRepository {
    fun getSamples(workoutId: String): Flow<List<HeartRateSample>>
    suspend fun saveSample(sample: HeartRateSample)
    suspend fun saveSamples(samples: List<HeartRateSample>)
}
