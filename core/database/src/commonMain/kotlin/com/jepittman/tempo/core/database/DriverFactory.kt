package com.jepittman.tempo.core.database

import app.cash.sqldelight.db.SqlDriver

/** Creates the platform-specific SQLite [SqlDriver]. Implement per platform and inject. */
fun interface DriverFactory {
    fun createDriver(): SqlDriver
}
