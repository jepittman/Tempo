package com.jepittman.tempo

import com.jepittman.tempo.core.database.IosDriverFactory

fun createAppContainer(): AppContainer = AppContainer(IosDriverFactory())
