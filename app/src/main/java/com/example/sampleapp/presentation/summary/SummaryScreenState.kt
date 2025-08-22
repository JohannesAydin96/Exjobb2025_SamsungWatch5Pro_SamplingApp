package com.example.sampleapp.presentation.summary

import java.time.Duration


data class SummaryScreenState(
    val averageHeartRate: Double,
    val elapsedTime: Duration,
    val avgX: Float? = 0f,
    val avgY: Float? = 0f,
    val avgZ: Float? = 0f,
    val fileName: String? =""
)