package com.example.sampleapp.presentation

import com.example.sampleapp.data.ServiceState
import com.example.sampleapp.presentation.preparing.PreparingExerciseScreen
import com.example.sampleapp.presentation.preparing.PreparingScreenState
import com.example.sampleapp.presentation.preparing.PreparingViewModel
import com.example.sampleapp.service.ExerciseServiceState
import com.google.android.horologist.compose.ambient.AmbientState
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ResponsiveTimeText
import com.google.android.horologist.screenshots.FixedTimeSource
import com.google.android.horologist.screenshots.rng.WearDevice
import com.google.android.horologist.screenshots.rng.WearDeviceScreenshotTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class PreparingExerciseScreenTest(override val device: WearDevice) :
    WearDeviceScreenshotTest(device) {
    @Test
    fun preparing() = runTest {
        AppScaffold(
            timeText = { ResponsiveTimeText(timeSource = FixedTimeSource) }
        ) {
            PreparingExerciseScreen(
                ambientState = AmbientState.Interactive,
                onStart = {},
                uiState = PreparingScreenState.Preparing(
                    serviceState = ServiceState.Connected(
                        ExerciseServiceState()
                    ),
                    isTrackingInAnotherApp = false,
                    requiredPermissions = PreparingViewModel.permissions,
                    hasExerciseCapabilities = true
                )
            )
        }
    }

    @Test
    fun failed() = runTest {
        AppScaffold(
            timeText = { ResponsiveTimeText(timeSource = FixedTimeSource) }
        ) {
            PreparingExerciseScreen(
                ambientState = AmbientState.Ambient(),
                onStart = {},
                uiState = PreparingScreenState.Preparing(
                    serviceState = ServiceState.Connected(
                        ExerciseServiceState()
                    ),
                    isTrackingInAnotherApp = false,
                    requiredPermissions = PreparingViewModel.permissions,
                    hasExerciseCapabilities = true
                )
            )
        }
    }
}
