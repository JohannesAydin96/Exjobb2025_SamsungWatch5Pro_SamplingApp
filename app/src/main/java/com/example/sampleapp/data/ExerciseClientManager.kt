package com.example.sampleapp.data

import android.annotation.SuppressLint
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.ComparisonType
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeCondition
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseGoal
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseTypeCapabilities
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.LocationAvailability
import androidx.health.services.client.data.WarmUpConfig
import androidx.health.services.client.endExercise
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.markLap
import androidx.health.services.client.pauseExercise
import androidx.health.services.client.prepareExercise
import androidx.health.services.client.resumeExercise
import androidx.health.services.client.startExercise
import com.example.sampleapp.service.ExerciseLogger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Entry point for [HealthServicesClient] APIs, wrapping them in coroutine-friendly APIs.
 */
@SuppressLint("RestrictedApi")
@Singleton
class ExerciseClientManager @Inject constructor(
    healthServicesClient: HealthServicesClient,
    val logger: ExerciseLogger
) {
    val exerciseClient: ExerciseClient = healthServicesClient.exerciseClient

    suspend fun getExerciseCapabilities(): ExerciseTypeCapabilities? {
        val capabilities = exerciseClient.getCapabilities()

        return if (ExerciseType.RUNNING in capabilities.supportedExerciseTypes) {
            capabilities.getExerciseTypeCapabilities(ExerciseType.RUNNING)
        } else {
            null
        }
    }

    suspend fun startExercise() {
        logger.log("Starting exercise")
        // Types for which we want to receive metrics. Only ask for ones that are supported.
        val capabilities = getExerciseCapabilities()

        if (capabilities == null) {
            logger.log("No capabilities")
            return
        }

        val dataTypes = setOf(
            DataType.HEART_RATE_BPM,
            DataType.HEART_RATE_BPM_STATS,
        ).intersect(capabilities.supportedDataTypes)

        val exerciseGoals = mutableListOf<ExerciseGoal<Double>>()

        val supportsAutoPauseAndResume = capabilities.supportsAutoPauseAndResume

        val config = ExerciseConfig(
            exerciseType = ExerciseType.RUNNING,
            dataTypes = dataTypes,
            isAutoPauseAndResumeEnabled = false,
            isGpsEnabled = true,
            exerciseGoals = exerciseGoals
        )

        exerciseClient.startExercise(config)
        logger.log("Started exercise")
    }

    /***
     * Note: don't call this method from outside of ExerciseService.kt
     */
    suspend fun prepareExercise() {
        logger.log("Preparing an exercise")
        val warmUpConfig = WarmUpConfig(
            exerciseType = ExerciseType.RUNNING,
            dataTypes = setOf(DataType.HEART_RATE_BPM, DataType.LOCATION)
        )
        try {
            exerciseClient.prepareExercise(warmUpConfig)
        } catch (e: Exception) {
            logger.log("Prepare exercise failed - ${e.message}")
        }
    }

    suspend fun endExercise() {
        logger.log("Ending exercise")
        exerciseClient.endExercise()
    }

    suspend fun pauseExercise() {
        logger.log("Pausing exercise")
        exerciseClient.pauseExercise()
    }

    suspend fun resumeExercise() {
        logger.log("Resuming exercise")
        exerciseClient.resumeExercise()
    }


    /**
     * When the flow starts, it will register an [ExerciseUpdateCallback] and start to emit
     * messages. When there are no more subscribers, or when the coroutine scope is
     * cancelled, this flow will unregister the listener.
     * [callbackFlow] is used to bridge between a callback-based API and Kotlin flows.
     */
    val exerciseUpdateFlow = callbackFlow {
        val callback = object : ExerciseUpdateCallback {
            override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                trySendBlocking(ExerciseMessage.ExerciseUpdateMessage(update))
            }

            override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {
            }

            override fun onRegistered() {
            }

            override fun onRegistrationFailed(throwable: Throwable) {
                TODO("Not yet implemented")
            }

            override fun onAvailabilityChanged(
                dataType: DataType<*, *>, availability: Availability
            ) {
                if (availability is LocationAvailability) {
                    trySendBlocking(ExerciseMessage.LocationAvailabilityMessage(availability))
                }
            }
        }

        exerciseClient.setUpdateCallback(callback)
        awaitClose {
            // Ignore async result
            exerciseClient.clearUpdateCallbackAsync(callback)
        }
    }
}


sealed class ExerciseMessage {
    class ExerciseUpdateMessage(val exerciseUpdate: ExerciseUpdate) : ExerciseMessage()
    class LocationAvailabilityMessage(val locationAvailability: LocationAvailability) :
        ExerciseMessage()
}



