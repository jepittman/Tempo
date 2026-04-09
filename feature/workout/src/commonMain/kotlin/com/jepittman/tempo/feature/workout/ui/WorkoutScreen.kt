package com.jepittman.tempo.feature.workout.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jepittman.tempo.core.domain.model.WorkoutStatus
import com.jepittman.tempo.core.domain.model.WorkoutType
import com.jepittman.tempo.feature.workout.WorkoutIntent
import com.jepittman.tempo.feature.workout.WorkoutUiState
import com.jepittman.tempo.feature.workout.WorkoutViewModel

@Composable
fun WorkoutScreen(viewModel: WorkoutViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showIdle by remember { mutableStateOf(false) }

    if (showIdle) {
        IdleContent(onStart = { type, title ->
            showIdle = false
            viewModel.onIntent(WorkoutIntent.Start(type, title))
        })
        return
    }

    when (val state = uiState) {
        is WorkoutUiState.Idle -> IdleContent(
            onStart = { type, title -> viewModel.onIntent(WorkoutIntent.Start(type, title)) },
        )
        is WorkoutUiState.Starting -> StartingContent()
        is WorkoutUiState.Active -> ActiveContent(
            state = state,
            onIntent = { viewModel.onIntent(it) },
        )
        is WorkoutUiState.Completed -> CompletedContent(onDone = { showIdle = true })
        is WorkoutUiState.Discarded -> DiscardedContent(onDone = { showIdle = true })
        is WorkoutUiState.Error -> ErrorContent(
            message = state.message,
            onDismiss = { showIdle = true },
        )
    }
}

@Composable
fun IdleContent(onStart: (WorkoutType, String?) -> Unit) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<WorkoutType?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "New Workout",
            style = MaterialTheme.typography.headlineMedium,
        )
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Text(
            text = "Select workout type",
            style = MaterialTheme.typography.labelLarge,
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(WorkoutType.entries) { type ->
                WorkoutTypeCard(
                    type = type,
                    onClick = { selectedType = type },
                )
            }
        }
        Button(
            onClick = {
                selectedType?.let { type ->
                    onStart(type, title.ifBlank { null })
                }
            },
            enabled = selectedType != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Start")
        }
    }
}

@Composable
fun WorkoutTypeCard(type: WorkoutType, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = type.name,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun ActiveContent(state: WorkoutUiState.Active, onIntent: (WorkoutIntent) -> Unit) {
    val isPaused = state.activeWorkout.status == WorkoutStatus.Paused

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = state.activeWorkout.workout.type.name,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = state.formattedDuration,
            style = MaterialTheme.typography.displayLarge,
        )
        state.activeWorkout.currentHeartRateBpm?.let { bpm ->
            Text(
                text = "$bpm BPM",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        SuggestionChip(
            onClick = {},
            label = { Text(if (isPaused) "Paused" else "Active") },
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isPaused) {
                Button(
                    onClick = { onIntent(WorkoutIntent.Resume) },
                    modifier = Modifier.weight(1f),
                ) { Text("Resume") }
            } else {
                OutlinedButton(
                    onClick = { onIntent(WorkoutIntent.Pause) },
                    modifier = Modifier.weight(1f),
                ) { Text("Pause") }
            }
            Button(
                onClick = { onIntent(WorkoutIntent.Finish) },
                modifier = Modifier.weight(1f),
            ) { Text("Finish") }
            OutlinedButton(
                onClick = { onIntent(WorkoutIntent.Discard) },
                modifier = Modifier.weight(1f),
            ) { Text("Discard") }
        }
    }
}

@Composable
fun CompletedContent(onDone: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = "Workout Complete \u2713",
                style = MaterialTheme.typography.headlineMedium,
            )
            Button(onClick = onDone) {
                Text("Done")
            }
        }
    }
}

@Composable
fun DiscardedContent(onDone: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = "Workout discarded",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onDone) {
                Text("Done")
            }
        }
    }
}

@Composable
fun ErrorContent(message: String, onDismiss: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
fun StartingContent() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        CircularProgressIndicator()
    }
}
