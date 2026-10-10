package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.KeyEventBus
import com.example.data.TestRepository
import com.example.model.TestCategory
import com.example.model.TestStatus
import com.example.ui.components.AmbientBackground
import com.example.ui.components.InfoRow
import com.example.ui.components.StatusChip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

data class GuidedStepDef(
    val id: String,
    val category: TestCategory,
    @androidx.annotation.StringRes val titleRes: Int,
    @androidx.annotation.StringRes val descRes: Int,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullGuidedTestScreen(
    onNavigateBack: () -> Unit,
    repository: TestRepository = TestRepository.instance
) {
    val context = LocalContext.current

    val steps = remember {
        listOf(
            GuidedStepDef("display_colors", TestCategory.DISPLAY, R.string.guided_display_colors, R.string.guided_display_colors_desc, Icons.Default.Tv),
            GuidedStepDef("touch_grid", TestCategory.DISPLAY, R.string.guided_touch_grid, R.string.guided_touch_grid_desc, Icons.Default.Speed),
            GuidedStepDef("audio_tone", TestCategory.AUDIO, R.string.guided_audio_tone, R.string.guided_audio_tone_desc, Icons.Default.Headphones),
            GuidedStepDef("mic_record", TestCategory.AUDIO, R.string.guided_mic_record, R.string.guided_mic_record_desc, Icons.Default.Mic),
            GuidedStepDef("buttons", TestCategory.BUTTONS, R.string.guided_buttons, R.string.guided_buttons_desc, Icons.Default.SmartButton),
            GuidedStepDef("camera_torch", TestCategory.CAMERA, R.string.guided_camera_torch, R.string.guided_camera_torch_desc, Icons.Default.CameraAlt),
            GuidedStepDef("vibration", TestCategory.VIBRATION, R.string.guided_vibration, R.string.guided_vibration_desc, Icons.Default.Vibration),
            GuidedStepDef("battery", TestCategory.BATTERY, R.string.guided_battery, R.string.guided_battery_desc, Icons.Default.BatteryChargingFull),
            GuidedStepDef("sensors", TestCategory.SENSORS, R.string.guided_sensors, R.string.guided_sensors_desc, Icons.Default.Sensors),
            GuidedStepDef("connectivity", TestCategory.CONNECTIVITY, R.string.guided_connectivity, R.string.guided_connectivity_desc, Icons.Default.Wifi),
            GuidedStepDef("biometrics", TestCategory.BIOMETRICS, R.string.guided_biometrics, R.string.guided_biometrics_desc, Icons.Default.Fingerprint),
            GuidedStepDef("system", TestCategory.SYSTEM, R.string.guided_system, R.string.guided_system_desc, Icons.Default.Speed)
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    var isCompleted by remember { mutableStateOf(false) }
    val stepResults = remember { mutableStateMapOf<Int, TestStatus>() }

    fun recordAndAdvance(status: TestStatus) {
        stepResults[currentStepIndex] = status
        repository.setStatus(steps[currentStepIndex].category, status)
        if (currentStepIndex < steps.size - 1) {
            currentStepIndex++
        } else {
            isCompleted = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isCompleted) {
                        Text(stringResource(R.string.guided_summary_title))
                    } else {
                        val currentStep = steps[currentStepIndex]
                        Text(
                            text = "${currentStepIndex + 1}/${steps.size}: ${stringResource(currentStep.titleRes)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("nav_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (isCompleted) {
                        IconButton(onClick = { repository.shareReport(context) }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.export_report))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            if (!isCompleted) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { recordAndAdvance(TestStatus.PASSED) },
                                modifier = Modifier.weight(1f).testTag("guided_pass_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.guided_pass_btn))
                            }

                            Button(
                                onClick = { recordAndAdvance(TestStatus.FAILED) },
                                modifier = Modifier.weight(1f).testTag("guided_fail_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.guided_fail_btn))
                            }

                            OutlinedButton(
                                onClick = { recordAndAdvance(TestStatus.SKIPPED) },
                                modifier = Modifier.weight(1f).testTag("guided_skip_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.guided_skip_btn))
                            }
                        }

                        FilledTonalButton(
                            onClick = {
                                if (currentStepIndex < steps.size - 1) {
                                    currentStepIndex++
                                } else {
                                    isCompleted = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("guided_next_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (currentStepIndex == steps.size - 1) stringResource(R.string.action_finish) else stringResource(R.string.action_next))
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        AmbientBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isCompleted) {
                // Final Results Breakdown Table
                GuidedSummaryTableView(
                    steps = steps,
                    results = stepResults,
                    onRetest = {
                        stepResults.clear()
                        currentStepIndex = 0
                        isCompleted = false
                    },
                    onFinish = onNavigateBack,
                    onShare = { repository.shareReport(context) }
                )
            } else {
                // Fluid Animated Step Transitions
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val step = steps[currentStepIndex]

                    // Step Indicator Card with Progress
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(step.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Text(
                                        text = stringResource(step.titleRes),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "${currentStepIndex + 1} / ${steps.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = stringResource(step.descRes),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            LinearProgressIndicator(
                                progress = { (currentStepIndex + 1).toFloat() / steps.size },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }

                    // Integrated Clean Step Body with Animated Slide & Fade
                    AnimatedContent(
                        targetState = currentStepIndex,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut())
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> width } + fadeOut())
                            }
                        },
                        label = "guided_step_content",
                        modifier = Modifier.weight(1f)
                    ) { targetIdx ->
                        val currentStepDef = steps[targetIdx]
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            when (currentStepDef.id) {
                                "display_colors" -> StepDisplayColors()
                                "touch_grid" -> StepTouchGrid()
                                "audio_tone" -> StepAudioTone()
                                "mic_record" -> StepMicrophone(context)
                                "buttons" -> StepButtons()
                                "camera_torch" -> StepCamera()
                                "vibration" -> StepVibration(context)
                                "battery" -> StepBattery(context)
                                "sensors" -> StepSensors(context)
                                "connectivity" -> StepConnectivity(context)
                                "biometrics" -> StepBiometrics(context)
                                "system" -> StepSystem(context)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 1. Step: Display Colors Cycle
@Composable
private fun StepDisplayColors() {
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.White, Color.Black)
    val colorNames = listOf("Red (255, 0, 0)", "Green (0, 255, 0)", "Blue (0, 0, 255)", "White (255, 255, 255)", "Black (0, 0, 0)")
    var colorIdx by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .clickable { colorIdx = (colorIdx + 1) % colors.size },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors[colorIdx]),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.75f),
                modifier = Modifier.padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(colorNames[colorIdx], color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Tap to cycle next color • Check for dead pixels", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }
        }
    }
}

// 2. Step: Touch Grid Drawing
@Composable
private fun StepTouchGrid() {
    val cols = 8
    val rows = 12
    val covered = remember { mutableStateMapOf<Int, Boolean>() }
    val total = cols * rows

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val cellW = maxWidth.value / cols
            val cellH = maxHeight.value / rows

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val c = (change.position.x / (size.width / cols)).toInt().coerceIn(0, cols - 1)
                            val r = (change.position.y / (size.height / rows)).toInt().coerceIn(0, rows - 1)
                            covered[r * cols + c] = true
                        }
                    }
            ) {
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val idx = r * cols + c
                        val isHit = covered[idx] == true
                        drawRect(
                            color = if (isHit) Color(0xFF10B981) else Color.DarkGray.copy(alpha = 0.15f),
                            topLeft = Offset(c * (size.width / cols), r * (size.height / rows)),
                            size = Size(size.width / cols, size.height / rows)
                        )
                        drawRect(
                            color = Color.Gray.copy(alpha = 0.15f),
                            topLeft = Offset(c * (size.width / cols), r * (size.height / rows)),
                            size = Size(size.width / cols, size.height / rows),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "Swipe to color cells: ${covered.size} / $total (${(covered.size * 100) / total}%)",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// 3. Step: Audio Sine Tone
@Composable
private fun StepAudioTone() {
    var isPlaying by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var playbackJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(Unit) {
        onDispose { playbackJob?.cancel() }
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Headphones, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("440 Hz Reference Sine Tone", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Press play to test left & right loudspeaker clarity", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isPlaying) {
                        playbackJob?.cancel()
                        isPlaying = false
                    } else {
                        isPlaying = true
                        playbackJob = scope.launch(Dispatchers.Default) {
                            playPureTone(440f)
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = if (isPlaying) ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)) else ButtonDefaults.buttonColors()
            ) {
                Icon(if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (isPlaying) "Stop Sound" else "Play Sound")
            }
        }
    }
}

private suspend fun playPureTone(freq: Float) {
    val sampleRate = 44100
    val minBuf = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
    val track = AudioTrack.Builder()
        .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
        .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
        .setBufferSizeInBytes(minBuf * 2)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

    try {
        track.play()
        val buffer = ShortArray(minBuf)
        var phase = 0.0
        val inc = 2.0 * PI * freq / sampleRate
        while (kotlinx.coroutines.currentCoroutineContext().isActive) {
            for (i in buffer.indices) {
                buffer[i] = (sin(phase) * 32000).toInt().toShort()
                phase += inc
                if (phase > 2.0 * PI) phase -= 2.0 * PI
            }
            track.write(buffer, 0, buffer.size)
        }
    } finally {
        try { track.stop(); track.release() } catch (_: Exception) {}
    }
}

// 4. Step: Microphone Record 5s
@Composable
private fun StepMicrophone(context: Context) {
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission = it }

    var isRecording by remember { mutableStateOf(false) }
    var levelFrac by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("Microphone Input Test", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (!hasPermission) {
                Spacer(Modifier.height(16.dp))
                Button(onClick = { launcher.launch(Manifest.permission.RECORD_AUDIO) }, shape = RoundedCornerShape(12.dp)) {
                    Text("Grant Mic Permission")
                }
            } else {
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { levelFrac },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                    color = Color(0xFF10B981)
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        if (!isRecording) {
                            isRecording = true
                            scope.launch(Dispatchers.Default) {
                                val sRate = 44100
                                val bSize = AudioRecord.getMinBufferSize(sRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
                                val rec = AudioRecord(MediaRecorder.AudioSource.MIC, sRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bSize)
                                val buf = ShortArray(bSize / 2)
                                try {
                                    rec.startRecording()
                                    val start = System.currentTimeMillis()
                                    while (System.currentTimeMillis() - start < 4000 && isActive) {
                                        val read = rec.read(buf, 0, buf.size)
                                        if (read > 0) {
                                            var maxAmp = 0
                                            for (i in 0 until read) {
                                                val amp = Math.abs(buf[i].toInt())
                                                if (amp > maxAmp) maxAmp = amp
                                            }
                                            withContext(Dispatchers.Main) { levelFrac = (maxAmp / 32767f).coerceIn(0f, 1f) }
                                        }
                                    }
                                } finally {
                                    try { rec.stop(); rec.release() } catch (_: Exception) {}
                                    withContext(Dispatchers.Main) { isRecording = false; levelFrac = 0f }
                                }
                            }
                        }
                    },
                    enabled = !isRecording,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isRecording) "Recording…" else "Start 4s Audio Sampling")
                }
            }
        }
    }
}

// 5. Step: Buttons
@Composable
private fun StepButtons() {
    var upCount by remember { mutableIntStateOf(0) }
    var downCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        KeyEventBus.events.collect {
            if (it.isDown) {
                if (it.keyCode == KeyEvent.KEYCODE_VOLUME_UP) upCount++
                if (it.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) downCount++
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.SmartButton, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("Press Hardware Volume Keys", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = if (upCount > 0) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (upCount > 0) Color(0xFF10B981) else Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Vol Up (+)", fontWeight = FontWeight.Bold)
                        Text("$upCount times", fontFamily = FontFamily.Monospace)
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = if (downCount > 0) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (downCount > 0) Color(0xFF10B981) else Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Vol Down (-)", fontWeight = FontWeight.Bold)
                        Text("$downCount times", fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

// 6. Step: Camera
@Composable
private fun StepCamera() {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    var hasPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission = it }
    var camObj by remember { mutableStateOf<Camera?>(null) }
    var torchOn by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        if (!hasPermission) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Camera permission required")
                Spacer(Modifier.height(12.dp))
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) { Text("Grant Permission") }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        val view = PreviewView(ctx)
                        val providerFut = ProcessCameraProvider.getInstance(ctx)
                        providerFut.addListener({
                            try {
                                val provider = providerFut.get()
                                val preview = Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
                                val selector = CameraSelector.DEFAULT_BACK_CAMERA
                                provider.unbindAll()
                                camObj = provider.bindToLifecycle(lifecycle, selector, preview)
                            } catch (_: Exception) {}
                        }, ContextCompat.getMainExecutor(ctx))
                        view
                    },
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp))
                )

                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.7f)
                ) {
                    Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val next = !torchOn
                                camObj?.cameraControl?.enableTorch(next)
                                torchOn = next
                            },
                            colors = if (torchOn) ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300), contentColor = Color.Black) else ButtonDefaults.buttonColors()
                        ) {
                            Icon(Icons.Default.FlashOn, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(if (torchOn) "Torch ON" else "Torch OFF")
                        }
                    }
                }
            }
        }
    }
}

// 7. Step: Vibration
@Composable
private fun StepVibration(context: Context) {
    val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("Vibration Motor & Tactile Feedback", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                    else { @Suppress("DEPRECATION") vibrator.vibrate(200) }
                }) { Text("Pulse (200ms)") }
                OutlinedButton(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createOneShot(40, 255))
                    else { @Suppress("DEPRECATION") vibrator.vibrate(40) }
                }) { Text("Haptic Click") }
            }
        }
    }
}

// 8. Step: Battery
@Composable
private fun StepBattery(context: Context) {
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
    val plugged = (intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: 0) > 0

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("Battery & Power Status", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(14.dp))
            InfoRow("Battery Level", "$level%")
            InfoRow("Voltage", "$voltage mV (${String.format(Locale.US, "%.2f V", voltage / 1000f)})")
            InfoRow("Power Cable Plugged", if (plugged) "Yes (Charging)" else "No (On Battery)")
        }
    }
}

// 9. Step: Sensors
@Composable
private fun StepSensors(context: Context) {
    val sm = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    var accelValues by remember { mutableStateOf(floatArrayOf(0f, 0f, 0f)) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    accelValues = event.values.clone()
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        val s = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (s != null) sm.registerListener(listener, s, SensorManager.SENSOR_DELAY_UI)
        onDispose { sm.unregisterListener(listener) }
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Accelerometer Spirit Bubble", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    drawCircle(Color.Gray.copy(alpha = 0.2f))
                    drawCircle(Color.Gray.copy(alpha = 0.5f), style = Stroke(2.dp.toPx()))
                    val cx = size.width / 2 - (accelValues[0] / 9.8f) * 35.dp.toPx()
                    val cy = size.height / 2 + (accelValues[1] / 9.8f) * 35.dp.toPx()
                    drawCircle(Color(0xFF10B981), radius = 10.dp.toPx(), center = Offset(cx, cy))
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("X: ${String.format(Locale.US, "%.2f", accelValues[0])}  Y: ${String.format(Locale.US, "%.2f", accelValues[1])}  Z: ${String.format(Locale.US, "%.2f", accelValues[2])}", fontFamily = FontFamily.Monospace)
        }
    }
}

// 10. Step: Connectivity
@Composable
private fun StepConnectivity(context: Context) {
    val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? android.net.wifi.WifiManager
    @Suppress("DEPRECATION")
    val bt = android.bluetooth.BluetoothAdapter.getDefaultAdapter()

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("Radios & Network Status", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(14.dp))
            InfoRow("Wi-Fi Radio", if (wifi?.isWifiEnabled == true) "Enabled" else "Disabled")
            InfoRow("Bluetooth Radio", if (bt?.isEnabled == true) "Enabled" else "Disabled / Off")
            InfoRow("BLE Support", if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) "Yes" else "No")
        }
    }
}

// 11. Step: Biometrics
@Composable
private fun StepBiometrics(context: Context) {
    val bm = BiometricManager.from(context)
    val canAuth = bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    var authMsg by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("Biometric Authentication", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(if (canAuth) "Class 3 Biometric Sensor Active" else "Biometrics Enrolled or Hardware Available", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(20.dp))

            Button(onClick = {
                val act = context as? AppCompatActivity ?: return@Button
                val prompt = BiometricPrompt(act, ContextCompat.getMainExecutor(act), object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { authMsg = "Verified Successfully!" }
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { authMsg = "Cancelled: $errString" }
                })
                prompt.authenticate(
                    BiometricPrompt.PromptInfo.Builder().setTitle("Test Biometrics").setNegativeButtonText("Cancel").setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK).build()
                )
            }) {
                Text("Trigger System Biometric Prompt")
            }

            if (authMsg != null) {
                Spacer(Modifier.height(10.dp))
                Text(authMsg ?: "", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
            }
        }
    }
}

// 12. Step: System
@Composable
private fun StepSystem(context: Context) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("System & Platform Identity", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(14.dp))
            InfoRow("Device Model", Build.MODEL)
            InfoRow("Manufacturer", Build.MANUFACTURER)
            InfoRow("Android Version", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            InfoRow("CPU ABIs", Build.SUPPORTED_ABIS.joinToString(", "))
        }
    }
}

// Final Summary Table
@Composable
private fun GuidedSummaryTableView(
    steps: List<GuidedStepDef>,
    results: Map<Int, TestStatus>,
    onRetest: () -> Unit,
    onFinish: () -> Unit,
    onShare: () -> Unit
) {
    val passedCount = steps.indices.count { results[it] == TestStatus.PASSED }
    val failedCount = steps.indices.count { results[it] == TestStatus.FAILED }
    val skippedCount = steps.indices.count { results[it] == TestStatus.SKIPPED || results[it] == null }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(stringResource(R.string.guided_summary_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.guided_summary_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricPill(label = stringResource(R.string.guided_metric_passed), count = passedCount, color = Color(0xFF10B981), modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        MetricPill(label = stringResource(R.string.guided_metric_failed), count = failedCount, color = Color(0xFFEF4444), modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        MetricPill(label = stringResource(R.string.guided_metric_skipped), count = skippedCount, color = Color(0xFF6B7280), modifier = Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onShare, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.export_report))
                        }
                        OutlinedButton(onClick = onRetest, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.retest))
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.guided_table_headline),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }

        items(steps.indices.toList()) { idx ->
            val st = steps[idx]
            val status = results[idx] ?: TestStatus.SKIPPED
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(st.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Column {
                            Text("${idx + 1}. " + stringResource(st.titleRes), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(stringResource(st.descRes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                    }
                    StatusChip(status = status)
                }
            }
        }

        item {
            Button(onClick = onFinish, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), shape = RoundedCornerShape(14.dp)) {
                Text(stringResource(R.string.back))
            }
        }
    }
}

@Composable
private fun MetricPill(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = count.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}
