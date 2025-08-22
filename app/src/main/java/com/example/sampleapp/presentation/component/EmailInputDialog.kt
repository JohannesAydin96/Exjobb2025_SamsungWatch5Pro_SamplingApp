package com.example.sampleapp.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.wear.compose.material.MaterialTheme

@Composable
fun EmailInputDialog(
    onDismissRequest: () -> Unit,
    onEmailEntered: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = Color.DarkGray
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(color = Color.DarkGray),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Enter your Email ID")

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { onDismissRequest() }) {
                        Text("Cancel", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(onClick = {
                        onEmailEntered(email)
                        onDismissRequest()
                    }) {
                        Text("Submit", color = Color.White)
                    }
                }
            }
        }
    }
}