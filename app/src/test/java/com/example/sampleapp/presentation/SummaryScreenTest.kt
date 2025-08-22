package com.example.sampleapp.presentation

import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import com.example.sampleapp.presentation.summary.SummaryScreen
import com.example.sampleapp.presentation.summary.SummaryScreenState
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ResponsiveTimeText
import com.google.android.horologist.screenshots.FixedTimeSource
import com.google.android.horologist.screenshots.rng.WearDevice
import com.google.android.horologist.screenshots.rng.WearDeviceScreenshotTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import java.time.Duration

@RunWith(ParameterizedRobolectricTestRunner::class)
class SummaryScreenTest(override val device: WearDevice) : WearDeviceScreenshotTest(device) {
    @Test
    fun summary() {
        runTest {
            AppScaffold(
                timeText = { ResponsiveTimeText(timeSource = FixedTimeSource) }
            ) {
                SummaryScreen(
                    uiState = SummaryScreenState(
                        averageHeartRate = 75.0,
                        avgX = 1.0f,
                        avgY = 1.2f,
                        avgZ = 1.5f,
                        elapsedTime = Duration.ofMinutes(17).plusSeconds(1)
                    ),
                    onRestartClick = {},
                    onShareClick = {}
                )
            }
        }

        composeRule.onNode(hasScrollToIndexAction())
            .performTouchInput {
                repeat(10) {
                    swipeUp()
                }
            }

        captureScreenshot("_end")
    }

}
