package com.jepittman.tempo.feature.workout

/** Data Layer message paths shared between the phone and Wear OS companion. */
object WearablePaths {
    /** Phone → Watch: signals the watch to start capturing heart rate for a workout. */
    const val WORKOUT_START = "/tempo/workout/start"

    /** Phone → Watch: signals the watch to stop capturing heart rate. */
    const val WORKOUT_STOP = "/tempo/workout/stop"

    /** Watch → Phone: a single heart rate sample encoded as a 4-byte big-endian Int (BPM). */
    const val HEART_RATE_SAMPLE = "/tempo/heart_rate/sample"
}
