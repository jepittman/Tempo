package com.jepittman.tempo.feature.history

import com.jepittman.tempo.core.domain.model.WorkoutSummary

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Success(val summaries: List<WorkoutSummary>) : HistoryUiState
}
