package com.example.sampleapp.presentation.exercise

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.health.services.client.data.ExerciseState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sampleapp.data.HealthServicesRepository
import com.example.sampleapp.data.ServiceState
import com.example.sampleapp.presentation.component.calculateDuration
import com.example.sampleapp.presentation.component.createCsvInCache
import com.example.sampleapp.presentation.component.formatTotalDurationTime
import com.example.sampleapp.presentation.summary.SummaryScreenState
import com.example.sampleapp.service.ExerciseData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val healthServicesRepository: HealthServicesRepository
) : ViewModel() {

    private val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Target ~13 Hz: 1,000,000 µs / 13 ≈ 76,923 µs
    private val sensorFlow: Flow<Triple<Float, Float, Float>> = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(Triple(event.values[0], event.values[1], event.values[2]))
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        accelSensor?.also {
            val samplingPeriodUs = 76_923  // ≈13 Hz
            val maxReportLatencyUs = 0     // deliver immediately
            sensorManager.registerListener(listener, it, samplingPeriodUs, maxReportLatencyUs)
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }.conflate()

    val exerciseDataList = ArrayList<ExerciseData>()

    val uiState: StateFlow<ExerciseScreenState> = combine(
        healthServicesRepository.serviceState,
        sensorFlow
    ) { serviceState, (x, y, z) ->
        val exerciseState = (serviceState as? ServiceState.Connected)?.exerciseServiceState

        if (exerciseState?.exerciseState?.isPaused != true &&
            exerciseState?.exerciseState == ExerciseState.ACTIVE) {
            viewModelScope.launch {
                exerciseDataList.add(
                    ExerciseData(
                        date = System.currentTimeMillis(),
                        time = System.currentTimeMillis(),
                        duration = calculateDuration(exerciseState.activeDurationCheckpoint),
                        heartRate = exerciseState.exerciseMetrics?.heartRate ?: 0.0,
                        accelX = x,
                        accelY = y,
                        accelZ = z,
                    )
                )
            }
        }

        ExerciseScreenState(
            hasExerciseCapabilities = healthServicesRepository.hasExerciseCapability(),
            isTrackingAnotherExercise = healthServicesRepository.isTrackingExerciseInAnotherApp(),
            serviceState = serviceState,
            exerciseState = exerciseState,
            accelX = x,
            accelY = y,
            accelZ = z
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(3_000),
        healthServicesRepository.serviceState.value.let {
            ExerciseScreenState(
                true,
                false,
                it,
                (it as? ServiceState.Connected)?.exerciseServiceState,
                0f, 0f, 0f
            )
        }
    )

    suspend fun isExerciseInProgress(): Boolean {
        return healthServicesRepository.isExerciseInProgress()
    }

    fun startExercise() {
        healthServicesRepository.startExercise()
    }

    fun pauseExercise() {
        healthServicesRepository.pauseExercise()
    }

    fun endExercise() {
        healthServicesRepository.endExercise()
    }

    fun resumeExercise() {
        healthServicesRepository.resumeExercise()
    }

    fun getAvgX(): Float = exerciseDataList.mapNotNull { it.accelX }.average().toFloat()
    fun getAvgY(): Float = exerciseDataList.mapNotNull { it.accelY }.average().toFloat()
    fun getAvgZ(): Float = exerciseDataList.mapNotNull { it.accelZ }.average().toFloat()

    fun addAverageData(toSummary: SummaryScreenState): String {
        val ax = getAvgX()
        val ay = getAvgY()
        val az = getAvgZ()

        exerciseDataList.add(
            ExerciseData(
                date = System.currentTimeMillis(),
                time = System.currentTimeMillis(),
                duration = formatTotalDurationTime(toSummary.elapsedTime, includeSeconds = true),
                heartRate = toSummary.averageHeartRate,
                accelX = ax,
                accelY = ay,
                accelZ = az,
            )
        )
        return createCsvInCache(appContext, exerciseDataList)
    }
}
