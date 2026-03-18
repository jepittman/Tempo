package com.jepittman.tempo.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

/**
 * JVM driver factory. Pass [JdbcSqliteDriver.IN_MEMORY] as [url] in tests for an isolated
 * in-memory database, or a file path like `"jdbc:sqlite:tempo.db"` for persistent storage.
 */
/**
 * JVM implementation of [DriverFactory]. Pass [JdbcSqliteDriver.IN_MEMORY] as [url] in tests
 * for an isolated in-memory database, or a JDBC URL like `"jdbc:sqlite:tempo.db"` for
 * persistent storage.
 */
class JvmDriverFactory(private val url: String = "jdbc:sqlite:tempo.db") : DriverFactory {
    override fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver(url)
        TempoDatabase.Schema.create(driver)
        return driver
    }
}
