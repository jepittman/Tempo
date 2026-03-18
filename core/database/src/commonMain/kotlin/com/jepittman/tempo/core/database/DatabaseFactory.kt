package com.jepittman.tempo.core.database

/** Creates a fully-configured [TempoDatabase] with all column adapters wired up. */
class DatabaseFactory(private val driverFactory: DriverFactory) {

    fun create(): TempoDatabase = TempoDatabase(
        driver = driverFactory.createDriver(),
        WorkoutEntityAdapter = WorkoutEntity.Adapter(
            typeAdapter = WorkoutTypeAdapter,
            started_atAdapter = InstantAdapter,
            ended_atAdapter = InstantAdapter,
        ),
        HeartRateSampleEntityAdapter = HeartRateSampleEntity.Adapter(
            recorded_atAdapter = InstantAdapter,
            sourceAdapter = DataSourceAdapter,
        ),
        WorkoutSetEntityAdapter = WorkoutSetEntity.Adapter(
            completed_atAdapter = InstantAdapter,
        ),
        WorkoutSummaryEntityAdapter = WorkoutSummaryEntity.Adapter(
            typeAdapter = WorkoutTypeAdapter,
            started_atAdapter = InstantAdapter,
        ),
    )
}
