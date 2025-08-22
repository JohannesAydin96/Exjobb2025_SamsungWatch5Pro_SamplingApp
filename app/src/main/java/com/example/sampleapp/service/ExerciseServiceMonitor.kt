package com.example.sampleapp.service

import android.annotation.SuppressLint
import android.app.Service
import android.util.Log
import androidx.health.services.client.data.ExerciseUpdate
import com.example.sampleapp.data.ExerciseClientManager
import com.example.sampleapp.data.ExerciseMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class ExerciseServiceMonitor @Inject constructor(
    val exerciseClientManager: ExerciseClientManager,
    val service: Service
) {
    // TODO behind an interface
    val exerciseService = service as ExerciseService



    val exerciseServiceState = MutableStateFlow(
        ExerciseServiceState(
            exerciseState = null,
            exerciseMetrics = ExerciseMetrics()
        )
    )

    suspend fun monitor() {
        exerciseClientManager.exerciseUpdateFlow.collect {
            when (it) {
                is ExerciseMessage.ExerciseUpdateMessage ->
                    processExerciseUpdate(it.exerciseUpdate)

                is ExerciseMessage.LocationAvailabilityMessage ->
                    exerciseServiceState.update { oldState ->
                        oldState.copy(
                            locationAvailability = it.locationAvailability
                        )
                    }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun processExerciseUpdate(exerciseUpdate: ExerciseUpdate) {
        // Dismiss any ongoing activity notification.
        if (exerciseUpdate.exerciseStateInfo.state.isEnded) {
            exerciseService.removeOngoingActivityNotification()
        }

        exerciseServiceState.update { old ->
             old.copy(
                exerciseState = exerciseUpdate.exerciseStateInfo.state,
                exerciseMetrics = old.exerciseMetrics.update(exerciseUpdate.latestMetrics),
                activeDurationCheckpoint = exerciseUpdate.activeDurationCheckpoint
                    ?: old.activeDurationCheckpoint
            )
        }

        Log.e("zzz", "processExerciseUpdate: $exerciseUpdate")
    }
}