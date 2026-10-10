package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.TestRepository
import com.example.model.TestCategory
import com.example.model.TestStatus
import com.example.ui.components.InfoRow
import com.example.ui.components.TestControlBar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsTestScreen(
    onNavigateBack: () -> Unit,
    repository: TestRepository = TestRepository.instance
) {
    val context = LocalContext.current
    val testResults by repository.testResults.collectAsState()
    val currentStatus = testResults[TestCategory.SENSORS] ?: TestStatus.UNTESTED

    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val allSensors = remember { sensorManager.getSensorList(Sensor.TYPE_ALL) }

    val sensorReadings = remember { mutableStateMapOf<Int, FloatArray>() }

    val targetSensors = remember {
        listOf(
            Sensor.TYPE_ACCELEROMETER to R.string.sensor_accelerometer,
            Sensor.TYPE_GYROSCOPE to R.string.sensor_gyroscope,
            Sensor.TYPE_MAGNETIC_FIELD to R.string.sensor_magnetic,
            Sensor.TYPE_PROXIMITY to R.string.sensor_proximity,
            Sensor.TYPE_LIGHT to R.string.sensor_light,
            Sensor.TYPE_PRESSURE to R.string.sensor_barometer,
            Sensor.TYPE_STEP_COUNTER to R.string.sensor_step_counter
        )
    }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null) {
                    sensorReadings[event.sensor.type] = event.values.clone()
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        targetSensors.forEach { (type, _) ->
            val s = sensorManager.getDefaultSensor(type)
            if (s != null) {
                sensorManager.registerListener(listener, s, SensorManager.SENSOR_DELAY_UI)
            }
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.category_sensors)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("nav_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        bottomBar = {
            TestControlBar(
                currentStatus = currentStatus,
                onStatusChange = { newStatus -> repository.setStatus(TestCategory.SENSORS, newStatus) },
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Sensors,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.sensors_count, allSensors.size),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.category_sensors_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Cards for each key sensor
            targetSensors.forEach { (sensorType, titleRes) ->
                val sensor = sensorManager.getDefaultSensor(sensorType)
                val values = sensorReadings[sensorType]

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(titleRes),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (sensor != null) "ACTIVE" else stringResource(R.string.not_available),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (sensor != null) Color(0xFF00C853) else MaterialTheme.colorScheme.error
                            )
                        }

                        if (sensor != null) {
                            InfoRow(label = "Vendor", value = sensor.vendor)
                            InfoRow(label = stringResource(R.string.sensor_range, sensor.maximumRange), value = String.format(Locale.US, "%.1f", sensor.maximumRange))

                            if (values != null) {
                                when (sensorType) {
                                    Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE, Sensor.TYPE_MAGNETIC_FIELD -> {
                                        val unit = when (sensorType) {
                                            Sensor.TYPE_ACCELEROMETER -> "m/s²"
                                            Sensor.TYPE_GYROSCOPE -> "rad/s"
                                            else -> "µT"
                                        }
                                        val x = values.getOrNull(0) ?: 0f
                                        val y = values.getOrNull(1) ?: 0f
                                        val z = values.getOrNull(2) ?: 0f

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            MetricBox(axis = "X", value = String.format(Locale.US, "%.2f", x), unit = unit, modifier = Modifier.weight(1f))
                                            MetricBox(axis = "Y", value = String.format(Locale.US, "%.2f", y), unit = unit, modifier = Modifier.weight(1f))
                                            MetricBox(axis = "Z", value = String.format(Locale.US, "%.2f", z), unit = unit, modifier = Modifier.weight(1f))
                                        }

                                        // Mini bubble visualization for accelerometer tilt
                                        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(60.dp)
                                                    .padding(top = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Canvas(modifier = Modifier.size(60.dp)) {
                                                    drawCircle(color = Color.Gray.copy(alpha = 0.3f), style = Stroke(width = 2.dp.toPx()))
                                                    val normX = (x / 9.8f).coerceIn(-1f, 1f)
                                                    val normY = (y / 9.8f).coerceIn(-1f, 1f)
                                                    val center = Offset(size.width / 2, size.height / 2)
                                                    val bubbleCenter = Offset(
                                                        center.x - normX * (size.width / 2 - 8.dp.toPx()),
                                                        center.y + normY * (size.height / 2 - 8.dp.toPx())
                                                    )
                                                    drawCircle(color = Color(0xFF00C853), radius = 6.dp.toPx(), center = bubbleCenter)
                                                }
                                            }
                                        }
                                    }
                                    Sensor.TYPE_PROXIMITY -> {
                                        val dist = values.getOrNull(0) ?: 0f
                                        val isNear = dist < 2.0f
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Distance: ${String.format(Locale.US, "%.1f cm", dist)}")
                                            Text(
                                                text = if (isNear) "NEAR" else "FAR",
                                                fontWeight = FontWeight.Bold,
                                                color = if (isNear) Color(0xFFE53935) else Color(0xFF00C853)
                                            )
                                        }
                                    }
                                    Sensor.TYPE_LIGHT -> {
                                        val lux = values.getOrNull(0) ?: 0f
                                        Text("Illuminance: ${String.format(Locale.US, "%.1f lx", lux)}", fontWeight = FontWeight.SemiBold)
                                    }
                                    Sensor.TYPE_PRESSURE -> {
                                        val hpa = values.getOrNull(0) ?: 0f
                                        Text("Pressure: ${String.format(Locale.US, "%.2f hPa", hpa)}", fontWeight = FontWeight.SemiBold)
                                    }
                                    Sensor.TYPE_STEP_COUNTER -> {
                                        val steps = values.getOrNull(0) ?: 0f
                                        Text("Total Steps: ${steps.toInt()}", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.sensor_not_present),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(axis: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(axis, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
