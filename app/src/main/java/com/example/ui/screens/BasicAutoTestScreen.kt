package com.example.ui.screens

import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.StringRes
import androidx.biometric.BiometricManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.TestRepository
import com.example.model.TestCategory
import com.example.model.TestStatus
import com.example.ui.components.AmbientBackground
import kotlinx.coroutines.delay

data class AutoCheckItem(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val detailRes: Int,
    val icon: ImageVector,
    val associatedCategory: TestCategory,
    var status: TestStatus = TestStatus.UNTESTED
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicAutoTestScreen(
    onNavigateBack: () -> Unit,
    repository: TestRepository = TestRepository.instance
) {
    val context = LocalContext.current
    var isRunning by remember { mutableStateOf(true) }
    var currentIndex by remember { mutableIntStateOf(0) }

    val checkItems = remember {
        mutableStateListOf(
            AutoCheckItem("battery", R.string.check_battery_name, R.string.check_battery_detail, Icons.Default.BatteryChargingFull, TestCategory.BATTERY),
            AutoCheckItem("accel", R.string.check_accel_name, R.string.check_accel_detail, Icons.Default.Speed, TestCategory.SENSORS),
            AutoCheckItem("gyro", R.string.check_gyro_name, R.string.check_gyro_detail, Icons.Default.Sensors, TestCategory.SENSORS),
            AutoCheckItem("magnetic", R.string.check_magnetic_name, R.string.check_magnetic_detail, Icons.Default.NearMe, TestCategory.SENSORS),
            AutoCheckItem("proximity", R.string.check_proximity_name, R.string.check_proximity_detail, Icons.Default.NearMe, TestCategory.SENSORS),
            AutoCheckItem("light", R.string.check_light_name, R.string.check_light_detail, Icons.Default.LightMode, TestCategory.SENSORS),
            AutoCheckItem("vibrator", R.string.check_vibrator_name, R.string.check_vibrator_detail, Icons.Default.Vibration, TestCategory.VIBRATION),
            AutoCheckItem("wifi", R.string.check_wifi_name, R.string.check_wifi_detail, Icons.Default.Wifi, TestCategory.CONNECTIVITY),
            AutoCheckItem("bt", R.string.check_bt_name, R.string.check_bt_detail, Icons.Default.Bluetooth, TestCategory.CONNECTIVITY),
            AutoCheckItem("ram", R.string.check_ram_name, R.string.check_ram_detail, Icons.Default.Memory, TestCategory.SYSTEM),
            AutoCheckItem("storage", R.string.check_storage_name, R.string.check_storage_detail, Icons.Default.SdStorage, TestCategory.SYSTEM),
            AutoCheckItem("biometrics", R.string.check_biometrics_name, R.string.check_biometrics_detail, Icons.Default.Fingerprint, TestCategory.BIOMETRICS),
            AutoCheckItem("cpu", R.string.check_cpu_name, R.string.check_cpu_detail, Icons.Default.Memory, TestCategory.SYSTEM)
        )
    }

    // Automated hardware evaluation worker
    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        @Suppress("DEPRECATION")
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val biometricManager = BiometricManager.from(context)

        val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        for (i in checkItems.indices) {
            currentIndex = i
            delay(240) // Smooth visual diagnostic cadence

            val item = checkItems[i]
            val passed: Boolean = when (item.id) {
                "battery" -> {
                    val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                    val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                    val health = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
                    level > 0 && health != BatteryManager.BATTERY_HEALTH_DEAD
                }
                "accel" -> sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
                "gyro" -> sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null
                "magnetic" -> sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null
                "proximity" -> sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null
                "light" -> sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT) != null
                "vibrator" -> {
                    val hasVib = vibrator.hasVibrator()
                    if (hasVib) {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator.vibrate(35)
                            }
                        } catch (_: Exception) {}
                    }
                    hasVib
                }
                "wifi" -> wifiManager != null
                "bt" -> bluetoothAdapter != null
                "ram" -> {
                    val memInfo = ActivityManager.MemoryInfo()
                    activityManager?.getMemoryInfo(memInfo)
                    memInfo.totalMem > 0
                }
                "storage" -> {
                    val stat = StatFs(Environment.getDataDirectory().path)
                    stat.blockCountLong > 0
                }
                "biometrics" -> {
                    val strong = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    val weak = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                    strong != BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE || weak != BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
                }
                "cpu" -> Runtime.getRuntime().availableProcessors() > 0 && Build.SUPPORTED_ABIS.isNotEmpty()
                else -> true
            }

            val newStatus = if (passed) TestStatus.PASSED else TestStatus.FAILED
            checkItems[i] = item.copy(status = newStatus)
            repository.setStatus(item.associatedCategory, newStatus)
        }

        isRunning = false
    }

    val passedCount = checkItems.count { it.status == TestStatus.PASSED }
    val failedCount = checkItems.count { it.status == TestStatus.FAILED }
    val totalCount = checkItems.size
    val testedCount = checkItems.count { it.status != TestStatus.UNTESTED }
    val progress = if (totalCount > 0) testedCount.toFloat() / totalCount else 0f

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
    val radarAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radar_alpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.mode_basic_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("nav_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { repository.shareReport(context) }) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.export_report))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        AmbientBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Status Banner Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isRunning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    // Properly colored indicator dot
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(
                                                color = if (failedCount > 0) Color(0xFFEF4444) else Color(0xFF10B981),
                                                shape = CircleShape
                                            )
                                    )
                                }
                                Text(
                                    text = if (isRunning) stringResource(R.string.auto_running_status) else stringResource(R.string.auto_completed_status),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (failedCount > 0) Color(0xFFEF4444).copy(alpha = 0.12f) else Color(0xFF10B981).copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, if (failedCount > 0) Color(0xFFEF4444).copy(alpha = 0.3f) else Color(0xFF10B981).copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "$passedCount PASS • $failedCount FAIL",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (failedCount > 0) Color(0xFFEF4444) else Color(0xFF10B981)
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (failedCount > 0 && !isRunning) Color(0xFFEF4444) else Color(0xFF10B981),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        if (!isRunning) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        checkItems.forEachIndexed { idx, it ->
                                            checkItems[idx] = it.copy(status = TestStatus.UNTESTED)
                                        }
                                        isRunning = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(stringResource(R.string.auto_retest))
                                }

                                Button(
                                    onClick = onNavigateBack,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(stringResource(R.string.close))
                                }
                            }
                        }
                    }
                }

                // List of Check Items with animated status reveal
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(checkItems) { index, item ->
                        AutoCheckRow(
                            item = item,
                            isActiveScanning = isRunning && index == currentIndex,
                            radarAlpha = radarAlpha
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoCheckRow(
    item: AutoCheckItem,
    isActiveScanning: Boolean,
    radarAlpha: Float
) {
    val borderColor = if (isActiveScanning) {
        MaterialTheme.colorScheme.primary.copy(alpha = radarAlpha)
    } else when (item.status) {
        TestStatus.PASSED -> Color(0xFF10B981).copy(alpha = 0.25f)
        TestStatus.FAILED -> Color(0xFFEF4444).copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActiveScanning)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
        ),
        border = BorderStroke(if (isActiveScanning) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = when (item.status) {
                    TestStatus.PASSED -> Color(0xFF10B981).copy(alpha = 0.12f)
                    TestStatus.FAILED -> Color(0xFFEF4444).copy(alpha = 0.12f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = when (item.status) {
                            TestStatus.PASSED -> Color(0xFF10B981)
                            TestStatus.FAILED -> Color(0xFFEF4444)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(item.nameRes),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(item.detailRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(8.dp))

            when (item.status) {
                TestStatus.PASSED -> {
                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                Text(
                                    text = "PASS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }
                }
                TestStatus.FAILED -> {
                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                                Text(
                                    text = "FAIL",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
                else -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isActiveScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(10.dp),
                                    strokeWidth = 1.5.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = "CHECK",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
