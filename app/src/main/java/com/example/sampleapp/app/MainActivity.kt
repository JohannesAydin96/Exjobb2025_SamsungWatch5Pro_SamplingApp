package com.example.sampleapp.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.sampleapp.presentation.ExerciseSampleApp
import com.example.sampleapp.presentation.exercise.ExerciseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private lateinit var navController: NavHostController
    private val exerciseViewModel by viewModels<ExerciseViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        var pendingNavigation = true

        splash.setKeepOnScreenCondition { pendingNavigation }

        super.onCreate(savedInstanceState)

        setContent {
            navController = rememberSwipeDismissableNavController()

            ExerciseSampleApp(
                navController,
                onFinishActivity = { this.finish() }
            )

            LaunchedEffect(Unit) {
                prepareIfNoExercise()
                pendingNavigation = false
            }
        }
    }

    private suspend fun prepareIfNoExercise() {
        /** Check if we have an active exercise. If true, set our destination as the
         * Exercise Screen. If false, route to preparing a new exercise. **/
        val isRegularLaunch =
            navController.currentDestination?.route == Screen.Exercise.route
        if (isRegularLaunch && !exerciseViewModel.isExerciseInProgress()) {
            navController.navigate(Screen.PreparingExercise.route)
        }
    }
}

