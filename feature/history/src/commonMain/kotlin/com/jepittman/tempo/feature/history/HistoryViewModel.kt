package com.jepittman.tempo.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jepittman.tempo.core.domain.usecase.GetWorkoutHistoryUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(
    getWorkoutHistory: GetWorkoutHistoryUseCase,
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = getWorkoutHistory()
        .map { HistoryUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState.Loading,
        )
}
