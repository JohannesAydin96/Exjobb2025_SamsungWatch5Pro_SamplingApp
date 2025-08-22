package com.example.sampleapp.service

import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseState
import androidx.health.services.client.data.ExerciseUpdate.ActiveDurationCheckpoint
import androidx.health.services.client.data.LocationAvailability

data class ExerciseMetrics(
    val heartRate: Double? = null,
    val heartRateAverage: Double? = null,
) {
    fun update(latestMetrics: DataPointContainer): ExerciseMetrics {

        return copy(
            heartRate = latestMetrics.getData(DataType.HEART_RATE_BPM).lastOrNull()?.value
                ?: heartRate,
            heartRateAverage = latestMetrics.getData(DataType.HEART_RATE_BPM_STATS)?.average
                ?: heartRateAverage
        )

    }
}

//Capturing most of the values associated with our exercise in a data class
data class ExerciseServiceState(
    val exerciseState: ExerciseState? = null,
    val exerciseMetrics: ExerciseMetrics = ExerciseMetrics(),
    val activeDurationCheckpoint: ActiveDurationCheckpoint? = null,
    val locationAvailability: LocationAvailability = LocationAvailability.UNKNOWN,
    val error: String? = null
    // NEW: current accelerometer reading
    /* val accelX: Float? = null,
     val accelY: Float? = null,
     val accelZ: Float? = null,*/
)

data class ExerciseData(
    val date: Long = 0,      // Time in milliseconds
    val time: Long = 0,      // Time in milliseconds
    val duration: String = "0h 0m 0s",      // Time in milliseconds
    val heartRate: Double? = 0.0,  // Heart rate in beats per minute (bpm)
    // NEW columns in the CSV:
    val accelX: Float? = null,
    val accelY: Float? = null,
    val accelZ: Float? = null,
)

