package com.example.sampleapp.presentation.component

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.example.sampleapp.R
import com.example.sampleapp.service.ExerciseData
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

fun createCsvInCache(context: Context, exerciseData: List<ExerciseData>): String {
    // Get the cache directory
    // val cacheDir = context.cacheDir
    val folder = File(context.cacheDir, "ExerciseHistory")
    if (!folder.exists()) {
        folder.mkdirs() // Create the directory if it does not exist
    }
    val name = "EXC_${getDateTime(System.currentTimeMillis())}"
    // Create the CSV file
    val csvFile = File(folder, "$name.csv")

    try {
        val header = "Date, Time, Duration, HeartRate(bpm), Ax(m/s²), Ay(m/s²), Az(m/s²)"
        FileWriter(csvFile).use { writer ->

            val title: String = context.getString(R.string.app_name)
            val model = Build.MODEL                    // e.g., "SM-R940"
            val androidVersion = Build.VERSION.RELEASE // e.g., "13"
            val sdkInt = Build.VERSION.SDK_INT         // e.g., 33

            writer.append(title)
            writer.append("\n")
            writer.append("Device: , $model")
            writer.append("\n")
            writer.append("Android Version: , $androidVersion (SDK $sdkInt) ")
            writer.append("\n")
            writer.append("Data collected: , Heart Rate and 3-Axis Acceleration")
            writer.append("\n")
            writer.append("Acceleration Unit: , meters per second squared (m/s²)")
            writer.append("\n")
            writer.append("Heart Rate Unit: , beats per minute (bpm)")
            writer.append("\n")
            writer.append("Sampling Frequency: , 13 Hz")
            writer.append("\n")
            writer.append("Notes: , Acceleration measured along device-fixed axes (X Y Z)")
            writer.append("\n")
            writer.append("\n")
            writer.append("\n")
            writer.append(header)
            writer.append("\n")
            val size = exerciseData.size
            exerciseData.forEachIndexed { index, it ->
                if (index == size - 1) {
                    // This is the last record, so mark it or modify it
                    writer.append("\n\n")
                    val dateTime = getDateAndTime(it.date)
                    val line = buildString {
                        append(dateTime.first)
                        append(", ")
                        append(" ${dateTime.second}")
                        append(", ")
                        append(it.duration)
                        append(", ")
                        append(formatToHeartRate(it.heartRate))
                        append(", ")
                        append(it.accelX ?: "")
                        append(", ")
                        append(it.accelY ?: "")
                        append(", ")
                        append(it.accelZ ?: "")
                    }
                    writer.appendLine(line)
                } else {
                    val dateTime = getDateAndTime(it.date)
                    val line = buildString {
                        append(dateTime.first)
                        append(", ")
                        append(dateTime.second)
                        append(", ")
                        append(it.duration)
                        append(", ")
                        append(formatToHeartRate(it.heartRate))
                        append(", ")
                        append(it.accelX ?: "")
                        append(", ")
                        append(it.accelY ?: "")
                        append(", ")
                        append(it.accelZ ?: "")
                    }
                    writer.appendLine(line)
                }
            }
        }
        println("CSV file created successfully at ${csvFile.absolutePath}")
    } catch (e: IOException) {
        Log.e("xxx", "exception ${e.message}")
        e.printStackTrace()
    }
    Log.d("CSV_DEBUG", "CSV created at: ${csvFile.absolutePath}, size: ${csvFile.length()} bytes")

    return name
}

fun fetchCsvFileFromFolder(context: Context, fileName: String?): File? {
    // Locate the folder inside the cache directory\
    if (fileName == null) {
        return null
    }

    val folder = File(context.cacheDir, "ExerciseHistory")

    // Locate the file inside the folder
    val csvFile = File(folder, fileName)

    // Check if the file exists before returning it
    return if (csvFile.exists()) csvFile else null
}

fun isOnline(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities =
        connectivityManager.activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
    if (capabilities != null) {
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
    return false
}


/** Format heart rate with a "bpm" suffix. */
fun formatToHeartRate(bpm: Double?): String {
    return if (bpm == null || bpm.isNaN()) {
        "--"
    } else {
        val formattedBpm = "%.0f".format(bpm)
        formattedBpm
    }
}


fun getDateAndTime(millis: Long): Pair<String, String> {
    val instant = Instant.ofEpochMilli(millis)
    val localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

    val dateString = localDateTime.format(dateFormatter)
  //  val timeString = localDateTime.format(timeFormatter)
  //  val timeString = "\"" + localDateTime.format(timeFormatter) + "\""
    val timeString = "${localDateTime.format(timeFormatter)}ms "  // Leading apostrophe


    return Pair(dateString, timeString)
}

fun getDateTime(millis: Long): String {
    val instant = Instant.ofEpochMilli(millis)
    val localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()

    val dateFormatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss")

    val dateString = localDateTime.format(dateFormatter)

    return dateString
}
