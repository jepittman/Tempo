package com.jepittman.tempo.feature.workout

import com.jepittman.tempo.core.domain.model.DataSource
import com.jepittman.tempo.core.domain.model.WorkoutType

/**
 * Every action that can mutate [WorkoutUiState].
 *
 * Intents originate from two sources:
 * - **User** – tapping buttons on the phone or watch UI.
 * - **System** – heart rate samples arriving from the Wear OS companion via the Data Layer.
 */
sealed interface WorkoutIntent {

    /** User chose a workout type and optionally supplied a title, then tapped Start. */
    data class Start(
        val type: WorkoutType,
        val title: String? = null,
    ) : WorkoutIntent

    /** User tapped Pause during an active workout. */
    data object Pause : WorkoutIntent

    /** User tapped Resume on a paused workout. */
    data object Resume : WorkoutIntent

    /** User tapped Finish; workout is saved and summarised. */
    data object Finish : WorkoutIntent

    /** User confirmed they want to discard the workout. */
    data object Discard : WorkoutIntent

    /**
     * A heart rate sample arrived from the Wear OS companion.
     *
     * @param bpm Beats per minute as measured by the watch sensor.
     * @param source Which sensor/device produced this reading.
     */
    data class HeartRateReceived(val bpm: Int, val source: DataSource) : WorkoutIntent
}
