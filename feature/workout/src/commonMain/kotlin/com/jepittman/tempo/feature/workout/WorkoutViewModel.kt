package com.jepittman.tempo.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jepittman.tempo.core.domain.model.ActiveWorkout
import com.jepittman.tempo.core.domain.model.DataSource
import com.jepittman.tempo.core.domain.model.HeartRateSample
import com.jepittman.tempo.core.domain.model.WorkoutStatus
import com.jepittman.tempo.core.domain.model.WorkoutType
import com.jepittman.tempo.core.domain.usecase.DiscardWorkoutUseCase
import com.jepittman.tempo.core.domain.usecase.FinishWorkoutUseCase
import com.jepittman.tempo.core.domain.usecase.LogHeartRateSampleUseCase
import com.jepittman.tempo.core.domain.usecase.PauseWorkoutUseCase
import com.jepittman.tempo.core.domain.usecase.ResumeWorkoutUseCase
import com.jepittman.tempo.core.domain.usecase.StartWorkoutUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

class WorkoutViewModel(
    private val startWorkout: StartWorkoutUseCase,
    private val pauseWorkout: PauseWorkoutUseCase,
    private val resumeWorkout: ResumeWorkoutUseCase,
    private val finishWorkout: FinishWorkoutUseCase,
    private val discardWorkout: DiscardWorkoutUseCase,
    private val logHeartRateSample: LogHeartRateSampleUseCase,
    private val incomingHeartRates: Flow<Pair<Int, DataSource>> = emptyFlow(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkoutUiState>(WorkoutUiState.Idle)
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            incomingHeartRates.collect { (bpm, source) ->
                onIntent(WorkoutIntent.HeartRateReceived(bpm, source))
            }
        }
    }

    fun onIntent(intent: WorkoutIntent) {
        viewModelScope.launch {
            when (intent) {
                is WorkoutIntent.Start -> handleStart(intent.type, intent.title)
                is WorkoutIntent.Pause -> handlePause()
                is WorkoutIntent.Resume -> handleResume()
                is WorkoutIntent.Finish -> handleFinish()
                is WorkoutIntent.Discard -> handleDiscard()
                is WorkoutIntent.HeartRateReceived -> handleHeartRate(intent.bpm, intent.source)
            }
        }
    }

    private suspend fun handleStart(type: WorkoutType, title: String?) {
        _uiState.update { WorkoutUiState.Starting }
        runCatching { startWorkout(type, title) }
            .onSuccess { activeWorkout ->
                val started = activeWorkout.copy(status = WorkoutStatus.Active)
                _uiState.update {
                    WorkoutUiState.Active(
                        activeWorkout = started,
                        formattedDuration = formatDuration(started.elapsedSeconds),
                    )
                }
                startTimer()
            }
            .onFailure { e ->
                _uiState.update { WorkoutUiState.Error(e.message ?: "Unknown error") }
            }
    }

    private fun handlePause() {
        val current = currentActiveState() ?: return
        runCatching { pauseWorkout(current.activeWorkout) }
            .onSuccess { paused ->
                timerJob?.cancel()
                timerJob = null
                _uiState.update { current.copy(activeWorkout = paused) }
            }
            .onFailure { e ->
                _uiState.update { WorkoutUiState.Error(e.message ?: "Unknown error") }
            }
    }

    private fun handleResume() {
        val current = currentActiveState() ?: return
        runCatching { resumeWorkout(current.activeWorkout) }
            .onSuccess { resumed ->
                _uiState.update { current.copy(activeWorkout = resumed) }
                startTimer()
            }
            .onFailure { e ->
                _uiState.update { WorkoutUiState.Error(e.message ?: "Unknown error") }
            }
    }

    private suspend fun handleFinish() {
        val current = currentActiveState() ?: return
        timerJob?.cancel()
        timerJob = null
        runCatching { finishWorkout(current.activeWorkout) }
            .onSuccess { _uiState.update { WorkoutUiState.Completed } }
            .onFailure { e ->
                _uiState.update { WorkoutUiState.Error(e.message ?: "Unknown error") }
            }
    }

    private suspend fun handleDiscard() {
        val current = currentActiveState() ?: return
        timerJob?.cancel()
        timerJob = null
        runCatching { discardWorkout(current.activeWorkout) }
            .onSuccess { _uiState.update { WorkoutUiState.Discarded } }
            .onFailure { e ->
                _uiState.update { WorkoutUiState.Error(e.message ?: "Unknown error") }
            }
    }

    private suspend fun handleHeartRate(bpm: Int, source: DataSource) {
        val current = currentActiveState() ?: return
        val sample = HeartRateSample(
            workoutId = current.activeWorkout.workout.id,
            bpm = bpm,
            recordedAt = Clock.System.now(),
            source = source,
        )
        runCatching { logHeartRateSample(sample) }
            .onSuccess {
                _uiState.update {
                    current.copy(
                        activeWorkout = current.activeWorkout.copy(currentHeartRateBpm = bpm),
                    )
                }
            }
            .onFailure { e ->
                _uiState.update { WorkoutUiState.Error(e.message ?: "Unknown error") }
            }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                val current = currentActiveState() ?: break
                if (current.activeWorkout.status != WorkoutStatus.Active) break
                val updated = current.activeWorkout.copy(
                    elapsedSeconds = current.activeWorkout.elapsedSeconds + 1L,
                )
                _uiState.update {
                    current.copy(
                        activeWorkout = updated,
                        formattedDuration = formatDuration(updated.elapsedSeconds),
                    )
                }
            }
        }
    }

    private fun currentActiveState(): WorkoutUiState.Active? =
        _uiState.value as? WorkoutUiState.Active

    private fun formatDuration(seconds: Long): String {
        val h = seconds / 3_600L
        val m = (seconds % 3_600L) / 60L
        val s = seconds % 60L
        val mm = m.toString().padStart(2, '0')
        val ss = s.toString().padStart(2, '0')
        return if (h > 0L) "$h:$mm:$ss" else "$mm:$ss"
    }

    private fun updateActiveWorkout(transform: (ActiveWorkout) -> ActiveWorkout) {
        val current = currentActiveState() ?: return
        val updated = transform(current.activeWorkout)
        _uiState.update {
            current.copy(
                activeWorkout = updated,
                formattedDuration = formatDuration(updated.elapsedSeconds),
            )
        }
    }
}
