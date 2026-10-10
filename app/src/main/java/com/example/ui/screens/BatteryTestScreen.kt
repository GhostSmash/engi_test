package com.example.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.History
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.draw.scale
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.TestRepository
import com.example.model.TestCategory
import com.example.model.TestStatus
import com.example.ui.components.InfoRow
import com.example.ui.components.TestControlBar
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PlugEvent(val timestamp: String, val message: String, val isConnected: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryTestScreen(
    onNavigateBack: () -> Unit,
    repository: TestRepository = TestRepository.instance
) {
    val context = LocalContext.current
    val testResults by repository.testResults.collectAsState()
    val currentStatus = testResults[TestCategory.BATTERY] ?: TestStatus.UNTESTED

    val batteryManager = remember { context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager }

    var batteryLevel by remember { mutableIntStateOf(0) }
    var batteryScale by remember { mutableIntStateOf(100) }
    var statusText by remember { mutableStateOf("") }
    var plugSourceText by remember { mutableStateOf("") }
    var voltageMv by remember { mutableIntStateOf(0) }
    var temperatureTenths by remember { mutableIntStateOf(0) }
    var healthText by remember { mutableStateOf("") }
    var technology by remember { mutableStateOf("") }
    var currentNowUa by remember { mutableIntStateOf(0) }
    var currentAvgUa by remember { mutableIntStateOf(0) }
    var chargeCounterUah by remember { mutableIntStateOf(0) }
    var isPlugged by remember { mutableStateOf<Boolean?>(null) }

    val plugEvents = remember { mutableStateListOf<PlugEvent>() }

    // Live update broadcast receiver
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                    batteryLevel = level
                    batteryScale = scale

                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    statusText = when (status) {
                        BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                        BatteryManager.BATTERY_STATUS_FULL -> "Full"
                        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                        else -> "Unknown"
                    }

                    val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                    val pluggedNow = plugged > 0
                    val source = when (plugged) {
                        BatteryManager.BATTERY_PLUGGED_AC -> "AC Charger"
                        BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
                        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Dock"
                        else -> "Unplugged (Battery)"
                    }
                    plugSourceText = source

                    voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                    temperatureTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                    technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

                    val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                    healthText = when (health) {
                        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
                        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                        else -> "Unknown"
                    }

                    // Detect live plug/unplug changes
                    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    if (isPlugged != null && isPlugged != pluggedNow) {
                        if (pluggedNow) {
                            plugEvents.add(0, PlugEvent(timeStr, "Connected to $source", true))
                        } else {
                            plugEvents.add(0, PlugEvent(timeStr, "Disconnected from charger", false))
                        }
                    } else if (isPlugged == null) {
                        // Initial event
                        plugEvents.add(0, PlugEvent(timeStr, if (pluggedNow) "Started while plugged ($source)" else "Started while on battery", pluggedNow))
                    }
                    isPlugged = pluggedNow
                }
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    // Coroutine to poll live BatteryManager properties (currentNow, currentAvg, chargeCounter)
    LaunchedEffect(Unit) {
        while (true) {
            currentNowUa = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            currentAvgUa = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
            chargeCounterUah = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            delay(1000)
        }
    }

    val batteryPercent = if (batteryScale > 0) (batteryLevel * 100) / batteryScale else batteryLevel
    val tempCelsius = temperatureTenths / 10.0
    val tempFahrenheit = tempCelsius * 9.0 / 5.0 + 32.0

    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "battery_pulse")
    val pulseCharging by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(durationMillis = 800),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulse_charge"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.category_battery)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("nav_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            TestControlBar(
                currentStatus = currentStatus,
                onStatusChange = { newStatus -> repository.setStatus(TestCategory.BATTERY, newStatus) },
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { innerPadding ->
        com.example.ui.components.AmbientBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Gauge Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$batteryPercent%",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (batteryPercent < 20) Color(0xFFEF4444) else Color(0xFF10B981)
                                )
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = if (isPlugged == true) Icons.Default.BatteryChargingFull else Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(54.dp)
                                    .then(if (isPlugged == true) Modifier.scale(pulseCharging) else Modifier),
                                tint = if (isPlugged == true) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                            )
                        }

                    LinearProgressIndicator(
                        progress = { (batteryPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (batteryPercent < 20) Color(0xFFE53935) else Color(0xFF00C853),
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }
            }

            // Detailed telemetry Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.sensor_live_values),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    InfoRow(label = stringResource(R.string.battery_plug_type), value = plugSourceText)
                    InfoRow(label = stringResource(R.string.battery_voltage), value = "$voltageMv mV (${String.format(Locale.US, "%.2f V", voltageMv / 1000f)})")
                    InfoRow(label = stringResource(R.string.battery_temp), value = String.format(Locale.US, "%.1f °C / %.1f °F", tempCelsius, tempFahrenheit))
                    InfoRow(label = stringResource(R.string.battery_health), value = healthText)
                    InfoRow(label = stringResource(R.string.battery_technology), value = technology)

                    if (currentNowUa != Int.MIN_VALUE && currentNowUa != 0) {
                        val currentMa = currentNowUa / 1000
                        InfoRow(label = stringResource(R.string.battery_current_now), value = "$currentMa mA")
                    }
                    if (currentAvgUa != Int.MIN_VALUE && currentAvgUa != 0) {
                        val avgMa = currentAvgUa / 1000
                        InfoRow(label = stringResource(R.string.battery_current_avg), value = "$avgMa mA")
                    }
                    if (chargeCounterUah > 0) {
                        val mah = chargeCounterUah / 1000
                        InfoRow(label = stringResource(R.string.battery_charge_counter), value = "$mah mAh")
                    }
                }
            }

            // Real-time plug events card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.battery_events_log),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (plugEvents.isEmpty()) {
                        Text(
                            text = stringResource(R.string.battery_waiting_events),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        plugEvents.take(8).forEach { ev ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (ev.isConnected) Icons.Default.Bolt else Icons.Default.Cable,
                                        contentDescription = null,
                                        tint = if (ev.isConnected) Color(0xFF00C853) else Color(0xFFE53935),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = ev.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = ev.timestamp,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

