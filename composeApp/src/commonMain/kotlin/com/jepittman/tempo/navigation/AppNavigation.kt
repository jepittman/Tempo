package com.jepittman.tempo.navigation

import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jepittman.tempo.AppContainer
import com.jepittman.tempo.feature.history.ui.HistoryScreen
import com.jepittman.tempo.feature.profile.ui.ProfileScreen
import com.jepittman.tempo.feature.workout.WorkoutViewModel
import com.jepittman.tempo.feature.workout.ui.WorkoutScreen
import com.jepittman.tempo.feature.history.HistoryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.Serializable

@Serializable data object WorkoutRoute
@Serializable data object HistoryRoute
@Serializable data object ProfileRoute

private data class TopLevelDestination(
    val route: Any,
    val label: String,
    val icon: @Composable () -> Unit,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(WorkoutRoute, "Workout") {
        Icon(Icons.Filled.FitnessCenter, contentDescription = "Workout")
    },
    TopLevelDestination(HistoryRoute, "History") {
        Icon(Icons.Filled.History, contentDescription = "History")
    },
    TopLevelDestination(ProfileRoute, "Profile") {
        Icon(Icons.Filled.Person, contentDescription = "Profile")
    },
)

@Composable
fun AppNavHost(
    navController: NavHostController,
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    NavHost(navController = navController, startDestination = WorkoutRoute, modifier = modifier) {
        composable<WorkoutRoute> {
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
        composable<HistoryRoute> {
            val vm = viewModel { HistoryViewModel(container.getWorkoutHistory) }
            HistoryScreen(vm)
        }
        composable<ProfileRoute> {
            ProfileScreen()
        }
    }
}

@Composable
fun AppNavigationBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar {
        topLevelDestinations.forEach { destination ->
            val selected = currentDestination?.hasRoute(destination.route::class) == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = destination.icon,
                label = { Text(destination.label) },
            )
        }
    }
}
