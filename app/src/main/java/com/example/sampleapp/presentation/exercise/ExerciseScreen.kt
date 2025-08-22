@file:OptIn(ExperimentalHorologistApi::class)

package com.example.sampleapp.presentation.exercise

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.example.sampleapp.R
import com.example.sampleapp.data.ServiceState
import com.example.sampleapp.presentation.component.HRText
import com.example.sampleapp.presentation.component.PauseButton
import com.example.sampleapp.presentation.component.ResumeButton
import com.example.sampleapp.presentation.component.StartButton
import com.example.sampleapp.presentation.component.StopButton
import com.example.sampleapp.presentation.component.formatElapsedTime
import com.example.sampleapp.presentation.summary.SummaryScreenState
import com.example.sampleapp.presentation.theme.ThemePreview
import com.example.sampleapp.service.ExerciseServiceState
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.ambient.AmbientState
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.AlertDialog
import com.google.android.horologist.health.composables.ActiveDurationText

@Composable
fun ExerciseRoute(
    ambientState: AmbientState,
    modifier: Modifier = Modifier,
    onSummary: (SummaryScreenState) -> Unit,
    onRestart: () -> Unit,
    onFinishActivity: () -> Unit,
) {
    val viewModel = hiltViewModel<ExerciseViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isEnded) {
        SideEffect {
            val fileName = viewModel.addAverageData(uiState.toSummary())
            onSummary(uiState.toSummaryWithFile(fileName,viewModel.getAvgX(), viewModel.getAvgY(), viewModel.getAvgZ()))
        }
    }

    if (uiState.error != null) {
        ErrorStartingExerciseScreen(
            onRestart = onRestart,
            onFinishActivity = onFinishActivity,
            uiState = uiState
        )
    } else if (ambientState is AmbientState.Interactive) {
        ExerciseScreen(
            onPauseClick = { viewModel.pauseExercise() },
            onEndClick = { viewModel.endExercise() },
            onResumeClick = { viewModel.resumeExercise() },
            onStartClick = { viewModel.startExercise() },
            uiState = uiState,
            modifier = modifier
        )
    }
}

/**
 * Shows an error that occured when starting an exercise
 */
@Composable
fun ErrorStartingExerciseScreen(
    onRestart: () -> Unit,
    onFinishActivity: () -> Unit,
    uiState: ExerciseScreenState
) {
    AlertDialog(
        title = stringResource(id = R.string.error_starting_exercise),
        message = "${uiState.error ?: stringResource(id = R.string.unknown_error)}. ${
            stringResource(
                id = R.string.try_again
            )
        }",
        onCancel = onFinishActivity,
        onOk = onRestart,
        showDialog = true,
    )
}

/**
 * Shows while an exercise is in progress
 */
@Composable
fun ExerciseScreen(
    onPauseClick: () -> Unit,
    onEndClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStartClick: () -> Unit,
    uiState: ExerciseScreenState,
    modifier: Modifier = Modifier
) {

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            modifier = modifier.fillMaxSize(),
            columnState = columnState
        ) {
            item {

                DurationRow(uiState)
            }

            item {
                HeartRateRow(uiState)
            }

            item {
                AccelDataRow(uiState)
            }

            item {
                ExerciseControlButtons(
                    uiState,
                    onStartClick,
                    onEndClick,
                    onResumeClick,
                    onPauseClick
                )
            }
        }
    }
}

@Composable
private fun ExerciseControlButtons(
    uiState: ExerciseScreenState,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    onResumeClick: () -> Unit,
    onPauseClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        if (uiState.isEnding) {
            StartButton(onStartClick)
        } else {
            StopButton(onEndClick)
        }

        if (uiState.isPaused) {
            ResumeButton(onResumeClick)
        } else {
            PauseButton(onPauseClick)
        }
    }
}

@Composable
private fun AccelDataRow(uiState: ExerciseScreenState){
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Ax: ${"%.1f".format(uiState.accelX ?: 0f)}")
        Text(text = "Ay: ${"%.1f".format(uiState.accelY ?: 0f)}")
        Text(text = "Az: ${"%.1f".format(uiState.accelZ ?: 0f)}")
    }
}

@Composable
private fun HeartRateRow(uiState: ExerciseScreenState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        Row {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = stringResource(id = R.string.heart_rate)
            )
            HRText(
                hr = uiState.exerciseState?.exerciseMetrics?.heartRate
            )
            Log.e(
                "zzz",
                "HeartRateAndCaloriesRow: ${uiState.exerciseState?.exerciseMetrics?.heartRate}"
            )
        }
    }
}

@Composable
private fun DurationRow(uiState: ExerciseScreenState) {
    val lastActiveDurationCheckpoint = uiState.exerciseState?.activeDurationCheckpoint
    val exerciseState = uiState.exerciseState?.exerciseState
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row {
            Icon(
                imageVector = Icons.Default.WatchLater,
                contentDescription = stringResource(id = R.string.duration)
            )
            if (exerciseState != null && lastActiveDurationCheckpoint != null) {
                ActiveDurationText(
                    checkpoint = lastActiveDurationCheckpoint,
                    state = uiState.exerciseState.exerciseState
                ) {
                    Text(text = formatElapsedTime(it, includeSeconds = true))
                }
            } else {
                Text(text = "--")
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun ExerciseScreenPreview() {
    ThemePreview {
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

@WearPreviewDevices
@Composable
fun ErrorStartingExerciseScreenPreview() {
    ThemePreview {
        ErrorStartingExerciseScreen(
            onRestart = {},
            onFinishActivity = {},
            uiState = ExerciseScreenState(
                hasExerciseCapabilities = true,
                isTrackingAnotherExercise = false,
                serviceState = ServiceState.Connected(
                    ExerciseServiceState()
                ),
                exerciseState = ExerciseServiceState(), 0f, 0f, 0f
            )
        )
    }
}
