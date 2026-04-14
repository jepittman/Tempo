package com.jepittman.tempo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.jepittman.tempo.navigation.AppNavHost
import com.jepittman.tempo.navigation.AppNavigationBar

@Composable
fun App(container: AppContainer) {
    MaterialTheme {
        val navController = rememberNavController()
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = { AppNavigationBar(navController) },
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                container = container,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
