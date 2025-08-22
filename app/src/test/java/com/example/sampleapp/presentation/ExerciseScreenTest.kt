package com.example.sampleapp.presentation

import com.example.sampleapp.data.ServiceState
import com.example.sampleapp.presentation.exercise.ExerciseScreen
import com.example.sampleapp.presentation.exercise.ExerciseScreenState
import com.example.sampleapp.service.ExerciseServiceState
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ResponsiveTimeText
import com.google.android.horologist.screenshots.FixedTimeSource
import com.google.android.horologist.screenshots.rng.WearDevice
import com.google.android.horologist.screenshots.rng.WearDeviceScreenshotTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class ExerciseScreenTest(override val device: WearDevice) : WearDeviceScreenshotTest(device) {
    @Test
    fun active() = runTest {
        AppScaffold(
            timeText = { ResponsiveTimeText(timeSource = FixedTimeSource) }
        ) {
            ExerciseScreen(
                onPauseClick = {},
                onEndClick = {},
                onResumeClick = {},
                onStartClick = {},
                uiState = ExerciseScreenState(
                    hasExerciseCapabilities = true,
                    isTrackingAnotherExercise = false,
                    serviceState = ServiceState.Connected(
                        ExerciseServiceState()
                    ),
                    exerciseState = ExerciseServiceState(),0f, 0f, 0f
                ),
            )
        }
    }
}
