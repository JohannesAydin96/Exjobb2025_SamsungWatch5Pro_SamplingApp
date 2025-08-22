package com.example.sampleapp.app

import androidx.navigation.NavController
import com.example.sampleapp.presentation.summary.SummaryScreenState

sealed class Screen(
    val route: String
) {
    object Exercise : Screen("exercise")
    object ExerciseNotAvailable : Screen("exerciseNotAvailable")
    object PreparingExercise : Screen("preparingExercise")
    object HistoryScreen : Screen("historyScreen")
    object Summary : Screen("summaryScreen") {
        fun buildRoute(summary: SummaryScreenState): String {
            return "$route/${summary.averageHeartRate}/${summary.elapsedTime}/${summary.avgX}/${summary.avgY}/${summary.avgZ}/${summary.fileName}"
        }

        val averageHeartRateArg = "averageHeartRate"
        val elapsedTimeArg = "elapsedTime"
        val avgX = "avgX"
        val avgY = "avgY"
        val avgZ = "avgZ"
        val fileName = "fileName"
    }
}

fun NavController.navigateToTopLevel(screen: Screen, route: String = screen.route) {
    navigate(route) {
        popUpTo(graph.id) {
            inclusive = true
        }
    }
}