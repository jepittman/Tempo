package com.jepittman.tempo.core.domain.usecase

import com.jepittman.tempo.core.domain.model.HeartRateSample
import com.jepittman.tempo.core.domain.repository.HeartRateRepository

class LogHeartRateSampleUseCase(private val repository: HeartRateRepository) {
    suspend operator fun invoke(sample: HeartRateSample) {
        repository.saveSample(sample)
    }
}
