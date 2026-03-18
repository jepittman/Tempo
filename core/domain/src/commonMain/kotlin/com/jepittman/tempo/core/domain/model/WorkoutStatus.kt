package com.jepittman.tempo.core.domain.model

enum class WorkoutStatus {
    /** Workout created but waiting — e.g. for GPS lock or watch connection. */
    Preparing,
    Active,
    Paused,
    Completed,
    Discarded,
}
