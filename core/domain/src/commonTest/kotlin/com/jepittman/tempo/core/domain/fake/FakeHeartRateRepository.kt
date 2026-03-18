package com.jepittman.tempo.core.domain.fake

import com.jepittman.tempo.core.domain.model.HeartRateSample
import com.jepittman.tempo.core.domain.repository.HeartRateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeHeartRateRepository : HeartRateRepository {

    private val store = MutableStateFlow<List<HeartRateSample>>(emptyList())

    override fun getSamples(workoutId: String): Flow<List<HeartRateSample>> =
        store.map { samples -> samples.filter { it.workoutId == workoutId } }

    override suspend fun saveSample(sample: HeartRateSample) {
        store.value = store.value + sample
    }

    override suspend fun saveSamples(samples: List<HeartRateSample>) {
        store.value = store.value + samples
    }
}
