package com.jepittman.tempo.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

/** iOS implementation of [DriverFactory] backed by SQLite via the native driver. */
class IosDriverFactory : DriverFactory {
    override fun createDriver(): SqlDriver =
        NativeSqliteDriver(TempoDatabase.Schema, "tempo.db")
}
