package com.jepittman.tempo.feature.workout

import com.jepittman.tempo.core.domain.model.ActiveWorkout

/**
 * Represents every possible state the workout screen can be in.
 *
 * The screen drives itself entirely off this sealed interface — there is no
 * separate "loading flag" field because each state is self-describing.
 */
sealed interface WorkoutUiState {

    /** No workout is running. The screen shows the start prompt. */
    data object Idle : WorkoutUiState

    /** A workout creation request has been sent; waiting for the repository. */
    data object Starting : WorkoutUiState

    /**
     * A workout is in progress (or paused). All live display values are derived
     * from [activeWorkout]; [formattedDuration] is pre-formatted to avoid
     * repeated string building in the UI layer.
     */
    data class Active(
        val activeWorkout: ActiveWorkout,
        val formattedDuration: String,
    ) : WorkoutUiState

    /** The workout finished successfully and has been persisted. */
    data object Completed : WorkoutUiState

    /** The workout was discarded and removed from storage. */
    data object Discarded : WorkoutUiState

    /** An unrecoverable error occurred; [message] is user-displayable. */
    data class Error(val message: String) : WorkoutUiState
}
