package com.example.sampleapp.presentation.exercise

import com.example.sampleapp.data.ServiceState
import com.example.sampleapp.presentation.summary.SummaryScreenState
import com.example.sampleapp.service.ExerciseServiceState
import java.time.Duration

data class ExerciseScreenState(
    val hasExerciseCapabilities: Boolean,
    val isTrackingAnotherExercise: Boolean,
    val serviceState: ServiceState,
    val exerciseState: ExerciseServiceState?,
    val accelX: Float?,
    val accelY: Float?,
    val accelZ: Float?
) {
    fun toSummary(): SummaryScreenState {
        val exerciseMetrics = exerciseState?.exerciseMetrics
        val averageHeartRate = exerciseMetrics?.heartRateAverage ?: Double.NaN
        val duration = exerciseState?.activeDurationCheckpoint?.activeDuration ?: Duration.ZERO
        return SummaryScreenState(averageHeartRate, duration)
    }

    fun toSummaryWithFile(fileName: String, avgX: Float, avgY: Float, avgZ: Float): SummaryScreenState {
        val exerciseMetrics = exerciseState?.exerciseMetrics
        val averageHeartRate = exerciseMetrics?.heartRateAverage ?: Double.NaN
        val duration = exerciseState?.activeDurationCheckpoint?.activeDuration ?: Duration.ZERO
        return SummaryScreenState(averageHeartRate, duration, avgX, avgY, avgZ,fileName)
    }

    val isEnding: Boolean
        get() = exerciseState?.exerciseState?.isEnding == true

    val isEnded: Boolean
        get() = exerciseState?.exerciseState?.isEnded == true

    val isPaused: Boolean
        get() = exerciseState?.exerciseState?.isPaused == true

    val error: String?
        get() = when(serviceState) {
            is ServiceState.Connected -> serviceState.exerciseServiceState.error
            else -> null
        }
}