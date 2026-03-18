package com.jepittman.tempo.core.database

import app.cash.sqldelight.ColumnAdapter
import com.jepittman.tempo.core.domain.model.DataSource
import com.jepittman.tempo.core.domain.model.WorkoutType
import kotlinx.datetime.Instant

internal object InstantAdapter : ColumnAdapter<Instant, Long> {
    override fun decode(databaseValue: Long): Instant = Instant.fromEpochMilliseconds(databaseValue)
    override fun encode(value: Instant): Long = value.toEpochMilliseconds()
}

internal object WorkoutTypeAdapter : ColumnAdapter<WorkoutType, String> {
    override fun decode(databaseValue: String): WorkoutType = WorkoutType.valueOf(databaseValue)
    override fun encode(value: WorkoutType): String = value.name
}

internal object DataSourceAdapter : ColumnAdapter<DataSource, String> {
    override fun decode(databaseValue: String): DataSource = DataSource.valueOf(databaseValue)
    override fun encode(value: DataSource): String = value.name
}
