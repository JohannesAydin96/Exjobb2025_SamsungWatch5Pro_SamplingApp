package com.example.sampleapp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.currentBackStackEntryAsState
import com.example.sampleapp.app.Screen.Exercise
import com.example.sampleapp.app.Screen.ExerciseNotAvailable
import com.example.sampleapp.app.Screen.HistoryScreen
import com.example.sampleapp.app.Screen.PreparingExercise
import com.example.sampleapp.app.Screen.Summary
import com.example.sampleapp.app.navigateToTopLevel
import com.example.sampleapp.presentation.dialogs.ExerciseNotAvailable
import com.example.sampleapp.presentation.exercise.ExerciseRoute
import com.example.sampleapp.presentation.history.HistoryRoute
import com.example.sampleapp.presentation.preparing.PreparingExerciseRoute
import com.example.sampleapp.presentation.summary.SummaryRoute
import com.google.android.horologist.compose.ambient.AmbientAware
import com.google.android.horologist.compose.ambient.AmbientState
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ResponsiveTimeText

/** Navigation for the exercise app. **/
@Composable
fun ExerciseSampleApp(
    navController: NavHostController,
    onFinishActivity: () -> Unit
) {
    val currentScreen by navController.currentBackStackEntryAsState()

    val isAlwaysOnScreen = currentScreen?.destination?.route in AlwaysOnRoutes

    AmbientAware(
        isAlwaysOnScreen = isAlwaysOnScreen
    ) { ambientStateUpdate ->

        AppScaffold(
            timeText = {
                if (ambientStateUpdate.ambientState is AmbientState.Interactive) {
                    ResponsiveTimeText()
                }
            }
        ) {
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = Exercise.route,

                ) {
                composable(PreparingExercise.route) {
                    PreparingExerciseRoute(
                        ambientState = ambientStateUpdate.ambientState,
                        onStart = {
                            navController.navigate(Exercise.route) {
                                popUpTo(navController.graph.id) {
                                    inclusive = false
                                }
                            }
                        },
                        onShow = {
                            navController.navigate(HistoryScreen.route) {
                                popUpTo(navController.graph.id) {
                                    inclusive = false
                                }
                            }
                        },
                        onNoExerciseCapabilities = {
                            navController.navigate(ExerciseNotAvailable.route) {
                                popUpTo(navController.graph.id) {
                                    inclusive = false
                                }
                            }
                        },
                        onFinishActivity = onFinishActivity
                    )
                }

                composable(HistoryScreen.route) {
                    HistoryRoute(
                        ambientState = ambientStateUpdate.ambientState,
                        onBack = {
                            navController.navigateToTopLevel(PreparingExercise)
                        },
                    )
                }

                composable(Exercise.route) {
                    ExerciseRoute(
                        ambientState = ambientStateUpdate.ambientState,
                        onSummary = {
                            navController.navigateToTopLevel(Summary, Summary.buildRoute(it))
                        },
                        onRestart = {
                            navController.navigateToTopLevel(PreparingExercise)
                        },
                        onFinishActivity = onFinishActivity
                    )
                }

                composable(ExerciseNotAvailable.route) {
                    ExerciseNotAvailable()
                }

                composable(
                    Summary.route + "/{averageHeartRate}/{elapsedTime}/{avgX}/{avgY}/{avgZ}/{fileName}",
                    arguments = listOf(
                        navArgument(Summary.averageHeartRateArg) { type = NavType.FloatType },
                        navArgument(Summary.elapsedTimeArg) { type = NavType.StringType },
                        navArgument(Summary.avgX) { type = NavType.FloatType },
                        navArgument(Summary.avgY) { type = NavType.FloatType },
                        navArgument(Summary.avgZ) { type = NavType.FloatType },
                        navArgument(Summary.fileName) { type = NavType.StringType }
                    )
                ) {
                    SummaryRoute(
                        onRestartClick = {
                            navController.navigateToTopLevel(PreparingExercise)
                        }
                    )
                }
            }
        }
    }
}

val AlwaysOnRoutes = listOf(PreparingExercise.route, Exercise.route)


