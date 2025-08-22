package com.example.sampleapp.service

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.health.services.client.data.ExerciseState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sampleapp.data.ExerciseClientManager
import com.example.sampleapp.data.isExerciseInProgress
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds


@AndroidEntryPoint
class ExerciseService : LifecycleService()/*, SensorEventListener */{

    @Inject
    lateinit var exerciseClientManager: ExerciseClientManager

    @Inject
    lateinit var exerciseNotificationManager: ExerciseNotificationManager

    @Inject
    lateinit var exerciseServiceMonitor: ExerciseServiceMonitor

    // === NEW: accelerometer fields ===
//    private lateinit var sensorManager: SensorManager
//    private var accelerometer: Sensor? = null

    /** Simple holder for each accel reading. */
    data class AccelData(val timestamp: Long, val x: Float, val y: Float, val z: Float)
    private val accelData = mutableListOf<AccelData>()

    private var isBound = false
    private var isStarted = false
    private val localBinder = LocalBinder()

    private val serviceRunningInForeground: Boolean
        get() = this.foregroundServiceType != ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE

    private suspend fun isExerciseInProgress() =
        exerciseClientManager.exerciseClient.isExerciseInProgress()

    /**
     * Prepare exercise in this service's coroutine context.
     */
    suspend fun prepareExercise() {
        exerciseClientManager.prepareExercise()
    }

    /**
     * Start exercise in this service's coroutine context.
     */
    suspend fun startExercise() {
        postOngoingActivityNotification()
        exerciseClientManager.startExercise()
    }

    /**
     * Pause exercise in this service's coroutine context.
     */
    suspend fun pauseExercise() {
        exerciseClientManager.pauseExercise()
    }

    /**
     * Resume exercise in this service's coroutine context.
     */
    suspend fun resumeExercise() {
        exerciseClientManager.resumeExercise()
    }

    /**
     * End exercise in this service's coroutine context.
     */
    suspend fun endExercise() {
        exerciseClientManager.endExercise()
        removeOngoingActivityNotification()
    }

    override fun onCreate() {
        super.onCreate()
        /* sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
         accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
         accelerometer?.let { sensor ->
             sensorManager.registerListener(
                 this,
                 sensor,
                 SensorManager.SENSOR_DELAY_FASTEST
             )
         }*/
    }

    override fun onDestroy() {
        super.onDestroy()
        // sensorManager.unregisterListener(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        Log.d(TAG, "onStartCommand")

        if (!isStarted) {
            isStarted = true

            if (!isBound) {
                // We may have been restarted by the system. Manage our lifetime accordingly.
                stopSelfIfNotRunning()
            }
            // Start collecting exercise information. We might stop shortly (see above), in which
            // case launchWhenStarted takes care of canceling this coroutine.
            lifecycleScope.launch(Dispatchers.Default) {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    exerciseServiceMonitor.monitor()
                }
            }
        }
        // If our process is stopped, we might have an active exercise. We want the system to
        // recreate our service so that we can present the ongoing notification in that case.
        return START_STICKY
    }

    private fun stopSelfIfNotRunning() {
        lifecycleScope.launch {
            // We may have been restarted by the system. Check for an ongoing exercise.
            if (!isExerciseInProgress()) {
                // Need to cancel [prepareExercise()] to prevent battery drain.
                if (exerciseServiceMonitor.exerciseServiceState.value.exerciseState == ExerciseState.PREPARING) {
                    lifecycleScope.launch {
                        endExercise()
                    }
                }
                // We have nothing to do, so we can stop.
                stopSelf()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)

        handleBind()

        return localBinder
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)

        handleBind()
    }

    private fun handleBind() {
        if (!isBound) {
            isBound = true
            // Start ourself. This will begin collecting exercise state if we aren't already.
            startService(Intent(this, this::class.java))
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isBound = false
        lifecycleScope.launch {
            // Client can unbind because it went through a configuration change, in which case it
            // will be recreated and bind again shortly. Wait a few seconds, and if still not bound,
            // manage our lifetime accordingly.
            delay(UNBIND_DELAY)
            if (!isBound) {
                stopSelfIfNotRunning()
            }
        }
        // Allow clients to re-bind. We will be informed of this in onRebind().
        return true
    }

    fun removeOngoingActivityNotification() {
        if (serviceRunningInForeground) {
            Log.d(TAG, "Removing ongoing activity notification")
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }

    private fun postOngoingActivityNotification() {
        if (!serviceRunningInForeground) {
            Log.d(TAG, "Posting ongoing activity notification")

            exerciseNotificationManager.createNotificationChannel()
            val serviceState = exerciseServiceMonitor.exerciseServiceState.value
            startForeground(
                ExerciseNotificationManager.NOTIFICATION_ID,
                exerciseNotificationManager.buildNotification(
                    serviceState.activeDurationCheckpoint?.activeDuration ?: Duration.ZERO
                )
            )
        }
    }

    /** Local clients will use this to access the service. */
    inner class LocalBinder : Binder() {
        fun getService() = this@ExerciseService

        val exerciseServiceState: Flow<ExerciseServiceState>
            get() = this@ExerciseService.exerciseServiceMonitor.exerciseServiceState
    }

    companion object {
        private val UNBIND_DELAY = 3.seconds
    }

    /*  override fun onSensorChanged(event: SensorEvent) {
          if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
              val x = event.values[0]
              val y = event.values[1]
              val z = event.values[2]
              // update the monitor's state
              Log.e(TAG, "onSensorChanged: $x $y $z", )
              this@ExerciseService.exerciseServiceMonitor.exerciseServiceState.update { old ->
                  old.copy(accelX = x, accelY = y, accelZ = z)
              }
          }
      }

      override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
      }*/
}
