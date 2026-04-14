package com.jepittman.tempo

import android.content.Context
import com.jepittman.tempo.core.database.AndroidDriverFactory

fun createAppContainer(context: Context): AppContainer = AppContainer(AndroidDriverFactory(context))
