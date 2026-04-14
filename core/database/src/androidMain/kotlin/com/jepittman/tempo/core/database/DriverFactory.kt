package com.jepittman.tempo.core.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/** Android implementation of [DriverFactory] backed by the room-less AndroidSqliteDriver. */
class AndroidDriverFactory(private val context: Context) : DriverFactory {
    override fun createDriver(): SqlDriver =
        AndroidSqliteDriver(TempoDatabase.Schema, context, "tempo.db")
}
