package com.jepittman.tempo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jepittman.tempo.feature.workout.WorkoutViewModel
import com.jepittman.tempo.feature.workout.ui.WorkoutScreen

@Composable
fun App(container: AppContainer) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val vm = viewModel {
                WorkoutViewModel(
                    startWorkout = container.startWorkout,
                    pauseWorkout = container.pauseWorkout,
                    resumeWorkout = container.resumeWorkout,
                    finishWorkout = container.finishWorkout,
                    discardWorkout = container.discardWorkout,
                    logHeartRateSample = container.logHeartRateSample,
                )
            }
            WorkoutScreen(vm)
        }
    }
}
