package com.jepittman.tempo

import com.jepittman.tempo.core.data.TempoDataModule
import com.jepittman.tempo.core.database.DriverFactory
import com.jepittman.tempo.core.domain.usecase.DiscardWorkoutUseCase
import com.jepittman.tempo.core.domain.usecase.FinishWorkoutUseCase
import com.jepittman.tempo.core.domain.usecase.LogHeartRateSampleUseCase
import com.jepittman.tempo.core.domain.usecase.PauseWorkoutUseCase
import com.jepittman.tempo.core.domain.usecase.ResumeWorkoutUseCase
import com.jepittman.tempo.core.domain.usecase.StartWorkoutUseCase

/**
 * Manual DI container. Create once per process with a platform-specific [DriverFactory]
 * and pass it to [App].
 */
class AppContainer(driverFactory: DriverFactory) {
    private val dataModule = TempoDataModule(driverFactory)

    val startWorkout = StartWorkoutUseCase(dataModule.workoutRepository)
    val pauseWorkout = PauseWorkoutUseCase()
    val resumeWorkout = ResumeWorkoutUseCase()
    val finishWorkout = FinishWorkoutUseCase(dataModule.workoutRepository, dataModule.heartRateRepository)
    val discardWorkout = DiscardWorkoutUseCase(dataModule.workoutRepository)
    val logHeartRateSample = LogHeartRateSampleUseCase(dataModule.heartRateRepository)
}
