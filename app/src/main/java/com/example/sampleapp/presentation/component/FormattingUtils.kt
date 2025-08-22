package com.example.sampleapp.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.health.services.client.data.ExerciseUpdate
import androidx.wear.compose.material.MaterialTheme
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

private val MINUTES_PER_HOUR = TimeUnit.HOURS.toMinutes(1)
private val SECONDS_PER_MINUTE = TimeUnit.MINUTES.toSeconds(1)

@Composable
fun formatElapsedTime(
    elapsedDuration: Duration?,
    includeSeconds: Boolean = false
) = buildAnnotatedString {
    if (elapsedDuration == null) {
        append("--")
    } else {
        val hours = elapsedDuration.toHours()
        if (hours > 0) {
            append(hours.toString())
            withStyle(style = MaterialTheme.typography.caption3.toSpanStyle()) {
                append("h")
            }
        }
        val minutes = elapsedDuration.toMinutes() % MINUTES_PER_HOUR
        append("%02d".format(minutes))
        withStyle(style = MaterialTheme.typography.caption3.toSpanStyle()) {
            append("m")
        }
        if (includeSeconds) {
            val seconds = elapsedDuration.seconds % SECONDS_PER_MINUTE
            append("%02d".format(seconds))
            withStyle(style = MaterialTheme.typography.caption3.toSpanStyle()) {
                append("s")
            }
        }
    }
}

fun formatTotalDurationTime(
    elapsedDuration: Duration?,
    includeSeconds: Boolean = false
): String {
    var totalTime = ""
    if (elapsedDuration == null) {
        return ""
    } else {
        val hours = elapsedDuration.toHours()
        if (hours > 0) {
            totalTime += "${hours}h"
        }
        val minutes = elapsedDuration.toMinutes() % MINUTES_PER_HOUR
        totalTime += " ${"%02d".format(minutes)}m"
        if (includeSeconds) {
            val seconds = elapsedDuration.seconds % SECONDS_PER_MINUTE
            totalTime += " ${"%02d".format(seconds)}s"
        }
        return totalTime
    }
}

fun calculateDuration(
    checkpoint: ExerciseUpdate.ActiveDurationCheckpoint?
): String {
    return if (checkpoint != null) {
        val delta = System.currentTimeMillis() - checkpoint.time.toEpochMilli()
        val elapsedDuration = checkpoint.activeDuration.plusMillis(delta)
        var totalTime = ""
        if (elapsedDuration == null) {
            ""
        } else {
            val hours = elapsedDuration.toHours()
            if (hours > 0) {
                totalTime += "${hours}h"
            }
            val minutes = elapsedDuration.toMinutes() % MINUTES_PER_HOUR
            totalTime += " ${"%02d".format(minutes)}m"
            val seconds = elapsedDuration.seconds % SECONDS_PER_MINUTE
            totalTime += " ${"%02d".format(seconds)}s"
            totalTime
        }
    } else "0h 0m 0s"

}


/** Format heart rate with a "bpm" suffix. */
@Composable
fun formatHeartRate(bpm: Double?) = buildAnnotatedString {
    if (bpm == null || bpm.isNaN()) {
        append("--")
    } else {
        append("%.0f".format(bpm))
        withStyle(style = MaterialTheme.typography.caption3.toSpanStyle()) {
            append("bpm")
        }
    }
}

/** Format heart rate with a "bpm" suffix. */
@Composable
fun formatAccelData(accel: Float?) = buildAnnotatedString {
    if (accel == null || accel.isNaN()) {
        append("--")
    } else {
        append("%.1f".format(accel))
        withStyle(style = MaterialTheme.typography.caption3.toSpanStyle()) {
            append("")
        }
    }
}
