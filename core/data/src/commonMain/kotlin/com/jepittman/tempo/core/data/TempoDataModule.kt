package com.jepittman.tempo.core.data

import com.jepittman.tempo.core.database.DatabaseFactory
import com.jepittman.tempo.core.database.DriverFactory
import com.jepittman.tempo.core.database.SqlDelightHeartRateRepository
import com.jepittman.tempo.core.database.SqlDelightWorkoutRepository
import com.jepittman.tempo.core.database.SqlDelightWorkoutSetRepository
import com.jepittman.tempo.core.domain.repository.HeartRateRepository
import com.jepittman.tempo.core.domain.repository.WorkoutRepository
import com.jepittman.tempo.core.domain.repository.WorkoutSetRepository

/**
 * Creates and wires the data layer from a platform-specific [DriverFactory].
 *
 * Instantiate once per process (e.g. in an AppContainer) and share the resulting
 * repository instances across the app.
 */
class TempoDataModule(driverFactory: DriverFactory) {
    private val database = DatabaseFactory(driverFactory).create()

    val workoutRepository: WorkoutRepository = SqlDelightWorkoutRepository(database)
    val heartRateRepository: HeartRateRepository = SqlDelightHeartRateRepository(database)
    val workoutSetRepository: WorkoutSetRepository = SqlDelightWorkoutSetRepository(database)
}
