@file:OptIn(ExperimentalHorologistApi::class)

package com.example.sampleapp.presentation.summary

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.example.sampleapp.R
import com.example.sampleapp.presentation.component.EmailInputDialog
import com.example.sampleapp.presentation.component.SummaryFormat
import com.example.sampleapp.presentation.component.fetchCsvFileFromFolder
import com.example.sampleapp.presentation.component.formatAccelData
import com.example.sampleapp.presentation.component.formatElapsedTime
import com.example.sampleapp.presentation.component.formatHeartRate
import com.example.sampleapp.presentation.component.isOnline
import com.example.sampleapp.presentation.theme.ThemePreview
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.ItemType
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.padding
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.ListHeaderDefaults.firstItemPadding
import com.google.android.horologist.compose.material.ResponsiveListHeader
import com.google.android.horologist.compose.material.Title
import java.io.File
import java.time.Duration

/**End-of-workout summary screen**/
@Composable
fun SummaryRoute(
    onRestartClick: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel = hiltViewModel<SummaryViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // State to hold the response status
    val isLoading by viewModel.isLoading
    val responseMessage by viewModel.responseMessage
    var showEmailDialog by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var selectedFile by remember { mutableStateOf<File?>(null) }


    LaunchedEffect(key1 = responseMessage) {
        responseMessage?.let {
            Toast.makeText(context, responseMessage, Toast.LENGTH_SHORT).show()
        }
    }


    if (showEmailDialog) {
        EmailInputDialog(
            onDismissRequest = { showEmailDialog = false },
            onEmailEntered = { enteredEmail ->
                email = enteredEmail
                if (isEmailValid(email)) {
                    if (isOnline(context)) {
                        selectedFile?.absolutePath?.let { viewModel.callSendDataApi(it, email) }
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.strNoInternet),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else
                    Toast.makeText(
                        context,
                        context.getString(R.string.strWarEnterValidEmail),
                        Toast.LENGTH_SHORT
                    ).show()
            }
        )
    }


    SummaryScreen(uiState = uiState, onRestartClick = onRestartClick, onShareClick = {
        Log.e("xxx", "file name ${uiState.fileName}")
        val file = fetchCsvFileFromFolder(context, "${uiState.fileName}.csv")
        file?.let {
            selectedFile = it
            showEmailDialog = true
            Log.e("xxx", "file>> ${file.name}")
        }
    })

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}


@Composable
fun SummaryScreen(
    uiState: SummaryScreenState,
    onRestartClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = padding(
            first = ItemType.Text,
            last = ItemType.Chip
        )
    )
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState
        ) {
            item {
                ResponsiveListHeader(contentPadding = firstItemPadding()) {
                    Title(text = stringResource(id = R.string.workout_complete))
                }
            }
            item {
                SummaryFormat(
                    value = formatElapsedTime(uiState.elapsedTime, includeSeconds = true),
                    metric = stringResource(id = R.string.duration),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                SummaryFormat(
                    value = formatHeartRate(uiState.averageHeartRate),
                    metric = stringResource(id = R.string.avgHR),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                SummaryFormat(
                    value = formatAccelData(uiState.avgX),
                    metric = stringResource(id = R.string.avgX),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                SummaryFormat(
                    value = formatAccelData(uiState.avgY),
                    metric = stringResource(id = R.string.avgY),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                SummaryFormat(
                    value = formatAccelData(uiState.avgZ),
                    metric = stringResource(id = R.string.avgZ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Chip(
                    label = stringResource(id = R.string.restart),
                    onClick = onRestartClick,
                )
            }

            item {
                Chip(
                    label = stringResource(id = R.string.strShare),
                    onClick = onShareClick,
                )
            }
        }
    }
}

fun isEmailValid(email: String): Boolean {
    if (email.isNullOrEmpty()) {
        return false
    }

    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
    return emailRegex.matches(email)
}


