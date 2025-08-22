package com.example.sampleapp.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Text

@Composable
fun HRText(hr: Double?) {
    Text(text = "${hr ?: "--"}")
}

@Preview
@Composable
fun HRTextPreview() {
    HRText(hr = 80.0)
}

@Preview
@Composable
fun HRTextPreviewMissing() {
    HRText(hr = null)
}
