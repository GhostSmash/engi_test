package com.example.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.DeviceHub
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.SmartButton
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R

enum class TestStatus {
    UNTESTED,
    PASSED,
    FAILED,
    SKIPPED
}

enum class TestCategory(
    val route: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val icon: ImageVector
) {
    DISPLAY(
        route = "display_test",
        titleRes = R.string.category_display,
        descriptionRes = R.string.category_display_desc,
        icon = Icons.Outlined.Tv
    ),
    AUDIO(
        route = "audio_test",
        titleRes = R.string.category_audio,
        descriptionRes = R.string.category_audio_desc,
        icon = Icons.Outlined.Headphones
    ),
    BATTERY(
        route = "battery_test",
        titleRes = R.string.category_battery,
        descriptionRes = R.string.category_battery_desc,
        icon = Icons.Outlined.BatteryChargingFull
    ),
    SENSORS(
        route = "sensors_test",
        titleRes = R.string.category_sensors,
        descriptionRes = R.string.category_sensors_desc,
        icon = Icons.Outlined.Sensors
    ),
    BUTTONS(
        route = "buttons_test",
        titleRes = R.string.category_buttons,
        descriptionRes = R.string.category_buttons_desc,
        icon = Icons.Outlined.SmartButton
    ),
    CAMERA(
        route = "camera_test",
        titleRes = R.string.category_camera,
        descriptionRes = R.string.category_camera_desc,
        icon = Icons.Outlined.CameraAlt
    ),
    VIBRATION(
        route = "vibration_test",
        titleRes = R.string.category_vibration,
        descriptionRes = R.string.category_vibration_desc,
        icon = Icons.Outlined.Vibration
    ),
    CONNECTIVITY(
        route = "connectivity_test",
        titleRes = R.string.category_connectivity,
        descriptionRes = R.string.category_connectivity_desc,
        icon = Icons.Outlined.DeviceHub
    ),
    BIOMETRICS(
        route = "biometrics_test",
        titleRes = R.string.category_biometrics,
        descriptionRes = R.string.category_biometrics_desc,
        icon = Icons.Outlined.Fingerprint
    ),
    SYSTEM(
        route = "system_test",
        titleRes = R.string.category_system,
        descriptionRes = R.string.category_system_desc,
        icon = Icons.Outlined.Info
    )
}
