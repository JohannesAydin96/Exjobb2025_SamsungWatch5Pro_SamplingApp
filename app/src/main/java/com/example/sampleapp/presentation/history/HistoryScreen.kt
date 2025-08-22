@file:OptIn(ExperimentalHorologistApi::class)

package com.example.sampleapp.presentation.history

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.sampleapp.R
import com.example.sampleapp.presentation.component.EmailInputDialog
import com.example.sampleapp.presentation.component.isOnline
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.ambient.AmbientState
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.AlertDialog
import java.io.File


@Composable
fun HistoryRoute(
    ambientState: AmbientState,
    onBack: () -> Unit,
) {
    val viewModel = hiltViewModel<HistoryViewModel>()

    val context = LocalContext.current
    var email by remember { mutableStateOf("") }

    // State to hold the list of files
    var filesList by remember { mutableStateOf<List<File>?>(null) }
    val isLoading by viewModel.isLoading
    val responseMessage by viewModel.responseMessage

    LaunchedEffect(Unit) {
        // Fetch all files from the folder
        filesList = viewModel.getAllFilesFromFolder(context)
    }

    var selectedFile by remember { mutableStateOf<File?>(null) }
    var fileToDelete by remember { mutableStateOf<File?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }

    if (showDialog && fileToDelete != null) {
        AlertDialog(
            title = "Delete File",
            message = "Are you sure you want to delete this file?",
            onCancel = { showDialog = false },
            onOk = {
                fileToDelete?.let { file ->
                    try {
                        val delete = viewModel.deleteFileFromFolder(context, file.name)
                        Log.e("delete", ">>> $delete")
                        filesList = viewModel.getAllFilesFromFolder(context)
                        Log.e("file list", ">>> $filesList")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    showDialog = false  // Close the dialog
                }
            },
            showDialog = showDialog,
        )
    }



    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    LaunchedEffect(key1 = responseMessage) {
        responseMessage?.let {
            Toast.makeText(context, responseMessage, Toast.LENGTH_SHORT).show()
        }
    }


    if (showEmailDialog) {
        EmailInputDialog(onDismissRequest = { showEmailDialog = false },
            onEmailEntered = { enteredEmail ->
                email = enteredEmail
                if (isEmailValid(email)) {
                    if (isOnline(context)) {
                        selectedFile?.absolutePath?.let { viewModel.callSendDataApi(it, email) }
                    } else {
                        Toast.makeText(
                            context, context.getString(R.string.strNoInternet), Toast.LENGTH_SHORT
                        ).show()
                    }
                } else Toast.makeText(
                    context, context.getString(R.string.strWarEnterValidEmail), Toast.LENGTH_SHORT
                ).show()
            })
    }


    if (ambientState is AmbientState.Interactive) {
        HistoryScreen(fileList = filesList, onDeleteClick = { item ->
            Log.e("delete file", ">>> ${item.name}")
            fileToDelete = item
            showDialog = true
        }, shareClick = { item ->
            selectedFile = item
            showEmailDialog = true
        }, onBack = { onBack() })
    }


}

fun isEmailValid(email: String): Boolean {
    if (email.isNullOrEmpty()) {
        return false
    }

    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
    return emailRegex.matches(email)
}


/**
 * Shows while an exercise is in progress
 */
@Composable
fun HistoryScreen(
    fileList: List<File>?,
    onDeleteClick: (File) -> Unit,
    shareClick: (File) -> Unit,
    onBack: () -> Unit
) {

    // State to track the file to be deleted and dialog visibility

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    ScreenScaffold(scrollState = columnState) {
        MyWearableList(items = fileList, onDeleteClick = { item ->
            // Handle item click (for preview purposes)
            onDeleteClick.invoke(item)
        }, shareClick = { item ->
            shareClick.invoke(item)
        }, onBack = { onBack() })
    }
}


@Composable
fun ListItem(item: File, onDeleteClick: () -> Unit, onShareClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Text(text = item.name, modifier = Modifier.weight(1.5f), maxLines = 2)
        // Spacer to add some space between text and icons
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = stringResource(id = R.string.strDelete),
            modifier = Modifier
                .size(22.dp)
                .padding(2.dp)
                .clickable(onClick = onDeleteClick)
        )

        Spacer(modifier = Modifier.width(8.dp)) // Adjust the width as needed

        // Share icon
        Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = stringResource(id = R.string.strShare),
            modifier = Modifier
                .size(22.dp)
                .padding(2.dp)
                .clickable(onClick = onShareClick)
        )

    }
}


@Composable
fun MyWearableList(
    items: List<File>?,
    onBack: () -> Unit,
    onDeleteClick: (File) -> Unit,
    shareClick: (File) -> Unit
) {
    // Create the state for the list
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    // A state to determine if the data is loaded
    var isLoading by remember { mutableStateOf(true) }

    // Simulate data loading (replace this with your actual data loading logic)
    LaunchedEffect(Unit) {
        // Simulating a delay for data loading (replace with your logic)
        isLoading = false // Set this to false once your data is loaded
    }


    when {
        isLoading -> {
            // Show loading indicator while data is loading
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        items.isNullOrEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "No history found", style = MaterialTheme.typography.body1)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onBack() }) {
                        Text(text = "Back")
                    }
                }
            }
        }

        else -> {
            items.let {
                val reversed = it.reversed()
                ScalingLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    columnState = columnState // Pass the correct state here
                ) {
                    items(reversed) { item ->
                        ListItem(item = item,
                            onDeleteClick = { onDeleteClick.invoke(item) },
                            onShareClick = {
                                Log.e("zzz", "onShareClick: ${item.name} ")
                                shareClick.invoke(item)
                            })
                    }

                    // Add a back button at the end of the list
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onBack() }, modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(text = "Back")
                        }
                    }
                }
            }
        }
    }

}






