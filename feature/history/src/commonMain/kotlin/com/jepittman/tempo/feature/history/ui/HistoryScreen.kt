package com.jepittman.tempo.feature.history.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jepittman.tempo.core.domain.model.WorkoutSummary
import com.jepittman.tempo.feature.history.HistoryUiState
import com.jepittman.tempo.feature.history.HistoryViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is HistoryUiState.Loading -> LoadingContent()
        is HistoryUiState.Success -> SuccessContent(state.summaries)
    }
}

@Composable
private fun LoadingContent() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SuccessContent(summaries: List<WorkoutSummary>) {
    if (summaries.isEmpty()) {
        EmptyContent()
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "History",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            items(summaries, key = { it.workoutId }) { summary ->
                WorkoutSummaryCard(summary)
            }
        }
    }
}

@Composable
private fun WorkoutSummaryCard(summary: WorkoutSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = summary.title ?: summary.type.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = summary.startedAt.formatDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider()

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StatItem(label = "Duration", value = summary.durationSeconds.formatDuration())
                summary.averageHeartRateBpm?.let {
                    StatItem(label = "Avg HR", value = "$it bpm")
                }
                summary.maxHeartRateBpm?.let {
                    StatItem(label = "Max HR", value = "$it bpm")
                }
                summary.totalSets?.let {
                    StatItem(label = "Sets", value = "$it")
                }
                summary.totalVolumeKg?.let {
                    StatItem(label = "Volume", value = "${it.formatVolume()} kg")
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyContent() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "No workouts yet", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Complete a workout to see it here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun Double.formatVolume(): String {
    val intPart = toLong()
    val dec = ((this - intPart) * 10).toLong()
    return "$intPart.$dec"
}

private fun Instant.formatDate(): String {
    val local = toLocalDateTime(TimeZone.currentSystemDefault())
    val month = local.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    return "$month ${local.dayOfMonth}, ${local.year}"
}

private fun Long.formatDuration(): String {
    val h = this / 3_600L
    val m = (this % 3_600L) / 60L
    val s = this % 60L
    val mm = m.toString().padStart(2, '0')
    val ss = s.toString().padStart(2, '0')
    return if (h > 0L) "$h:$mm:$ss" else "$mm:$ss"
}
