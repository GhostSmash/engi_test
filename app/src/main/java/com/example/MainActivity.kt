package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.KeyEventBus
import com.example.data.PreferencesManager
import com.example.model.TestCategory
import com.example.ui.screens.AudioTestScreen
import com.example.ui.screens.BasicAutoTestScreen
import com.example.ui.screens.BatteryTestScreen
import com.example.ui.screens.BiometricsTestScreen
import com.example.ui.screens.ButtonsTestScreen
import com.example.ui.screens.CameraTestScreen
import com.example.ui.screens.ConnectivityTestScreen
import com.example.ui.screens.DisplayTestScreen
import com.example.ui.screens.FullGuidedTestScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ModeSelectScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SensorsTestScreen
import com.example.ui.screens.SystemInfoScreen
import com.example.ui.screens.VibrationTestScreen
import com.example.ui.theme.DeviceTestTheme

class MainActivity : AppCompatActivity() {

    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        preferencesManager = PreferencesManager.getInstance(this)

        setContent {
            val themeMode by preferencesManager.themeMode.collectAsState()
            val isAmoled by preferencesManager.amoledMode.collectAsState()
            DeviceTestTheme(themeMode = themeMode, isAmoled = isAmoled) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DeviceTestApp(preferencesManager = preferencesManager)
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val consumed = KeyEventBus.onKeyEvent(keyCode, isDown = true)
        return if (consumed) true else super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        val consumed = KeyEventBus.onKeyEvent(keyCode, isDown = false)
        return if (consumed) true else super.onKeyUp(keyCode, event)
    }
}

@Composable
fun DeviceTestApp(preferencesManager: PreferencesManager) {
    val navController = rememberNavController()
    val isFirstLaunch by preferencesManager.isFirstLaunch.collectAsState()

    val startDest = if (isFirstLaunch) "onboarding" else "mode_select"

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable("onboarding") {
            OnboardingScreen(
                preferencesManager = preferencesManager,
                onFinish = {
                    navController.navigate("mode_select") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("settings") {
            OnboardingScreen(
                preferencesManager = preferencesManager,
                onFinish = {
                    navController.popBackStack()
                }
            )
        }

        composable("mode_select") {
            ModeSelectScreen(
                preferencesManager = preferencesManager,
                onNavigateToBasic = { navController.navigate("basic_test") },
                onNavigateToFull = { navController.navigate("full_guided_test") },
                onNavigateToManual = { navController.navigate("manual_home") },
                onOpenPreferences = { navController.navigate("settings") }
            )
        }

        composable("basic_test") {
            BasicAutoTestScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("full_guided_test") {
            FullGuidedTestScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("manual_home") {
            HomeScreen(
                preferencesManager = preferencesManager,
                onNavigateToCategory = { category ->
                    navController.navigate(category.route)
                },
                onOpenPreferences = {
                    navController.navigate("settings")
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(TestCategory.DISPLAY.route) {
            DisplayTestScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(TestCategory.AUDIO.route) {
            AudioTestScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(TestCategory.BATTERY.route) {
            BatteryTestScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(TestCategory.SENSORS.route) {
            SensorsTestScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(TestCategory.BUTTONS.route) {
            ButtonsTestScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(TestCategory.CAMERA.route) {
            CameraTestScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(TestCategory.VIBRATION.route) {
            VibrationTestScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(TestCategory.CONNECTIVITY.route) {
            ConnectivityTestScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(TestCategory.BIOMETRICS.route) {
            BiometricsTestScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(TestCategory.SYSTEM.route) {
            SystemInfoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
