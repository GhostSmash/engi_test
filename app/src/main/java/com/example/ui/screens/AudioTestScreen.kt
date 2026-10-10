package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.animation.core.animateFloat
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.TestRepository
import com.example.model.TestCategory
import com.example.model.TestStatus
import com.example.ui.components.TestControlBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioTestScreen(
    onNavigateBack: () -> Unit,
    repository: TestRepository = TestRepository.instance
) {
    val context = LocalContext.current
    val testResults by repository.testResults.collectAsState()
    val currentStatus = testResults[TestCategory.AUDIO] ?: TestStatus.UNTESTED

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.category_audio)) },
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
                onStatusChange = { newStatus -> repository.setStatus(TestCategory.AUDIO, newStatus) },
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
                modifier = Modifier.fillMaxSize()
            ) {
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(stringResource(R.string.audio_tones_title)) },
                        icon = { Icon(Icons.Default.GraphicEq, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(stringResource(R.string.audio_stereo_title)) },
                        icon = { Icon(Icons.Default.SurroundSound, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text(stringResource(R.string.audio_mic_title)) },
                        icon = { Icon(Icons.Default.Mic, contentDescription = null) }
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> SineToneGeneratorSection()
                        1 -> StereoChannelSection(context)
                        2 -> MicrophoneTestSection(context)
                    }
                }
            }
        }
    }
}

@Composable
private fun SineToneGeneratorSection() {
    var frequency by remember { mutableFloatStateOf(440f) }
    var isPlaying by remember { mutableStateOf(false) }
    var isSweeping by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var playbackJob by remember { mutableStateOf<Job?>(null) }

    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "wave_anim")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * kotlin.math.PI.toFloat(),
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(durationMillis = 600, easing = androidx.compose.animation.core.LinearEasing)
        ),
        label = "phase"
    )

    DisposableEffect(Unit) {
        onDispose {
            playbackJob?.cancel()
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.audio_tones_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.audio_tones_desc),
                style = MaterialTheme.typography.bodyMedium
            )

            // Animated Sine Wave Visualizer Box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                val waveColor = if (isPlaying) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                val strokePx = 2.dp
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val midY = h / 2
                    val amp = if (isPlaying) h * 0.35f else h * 0.08f
                    val cycles = 4f
                    val path = androidx.compose.ui.graphics.Path()

                    path.moveTo(0f, midY)
                    val steps = 60
                    for (i in 0..steps) {
                        val x = (i.toFloat() / steps) * w
                        val angle = (i.toFloat() / steps) * cycles * 2 * kotlin.math.PI.toFloat() + (if (isPlaying) wavePhase else 0f)
                        val y = midY + kotlin.math.sin(angle) * amp
                        path.lineTo(x, y.toFloat())
                    }
                    drawPath(path, color = waveColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokePx.toPx()))
                }
            }

            com.example.ui.components.M3SliderWithIcons(
                value = frequency,
                onValueChange = {
                    frequency = it
                    isSweeping = false
                },
                valueRange = 20f..20000f,
                startIcon = Icons.Default.GraphicEq,
                endIcon = Icons.Default.SurroundSound,
                valueLabel = "${frequency.toInt()} Hz",
                testTag = "frequency_slider"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf(100f, 440f, 1000f, 5000f, 10000f)
                presets.forEach { presetFreq ->
                    FilterChip(
                        selected = frequency.toInt() == presetFreq.toInt() && !isSweeping,
                        onClick = {
                            frequency = presetFreq
                            isSweeping = false
                        },
                        label = {
                            val text = if (presetFreq >= 1000f) "${(presetFreq / 1000).toInt()}k" else "${presetFreq.toInt()}"
                            Text(text)
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (isPlaying) {
                            playbackJob?.cancel()
                            isPlaying = false
                            isSweeping = false
                        } else {
                            isPlaying = true
                            isSweeping = false
                            playbackJob = scope.launch(Dispatchers.Default) {
                                playToneWorker { frequency }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("play_tone_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(if (isPlaying && !isSweeping) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (isPlaying && !isSweeping) stringResource(R.string.action_stop) else stringResource(R.string.action_start))
                }

                OutlinedButton(
                    onClick = {
                        if (isPlaying && isSweeping) {
                            playbackJob?.cancel()
                            isPlaying = false
                            isSweeping = false
                        } else {
                            playbackJob?.cancel()
                            isPlaying = true
                            isSweeping = true
                            playbackJob = scope.launch(Dispatchers.Default) {
                                playSweepWorker(
                                    onFreqUpdate = { f ->
                                        scope.launch(Dispatchers.Main) { frequency = f }
                                    },
                                    onComplete = {
                                        scope.launch(Dispatchers.Main) {
                                            isPlaying = false
                                            isSweeping = false
                                        }
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("sweep_tone_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isPlaying && isSweeping) stringResource(R.string.action_stop) else stringResource(R.string.audio_preset_sweep))
                }
            }
        }
    }
}

private suspend fun playToneWorker(freqProvider: () -> Float) {
    val sampleRate = 44100
    val minBufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    val bufferSize = (minBufferSize * 2).coerceAtLeast(sampleRate / 4)

    val track = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
        .setBufferSizeInBytes(bufferSize)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

    try {
        track.play()
        val buffer = ShortArray(bufferSize / 2)
        var phase = 0.0
        while (kotlinx.coroutines.currentCoroutineContext().isActive) {
            val freq = freqProvider()
            val phaseInc = 2.0 * PI * freq / sampleRate
            for (i in buffer.indices) {
                buffer[i] = (sin(phase) * 32000).toInt().toShort()
                phase += phaseInc
                if (phase > 2.0 * PI) phase -= 2.0 * PI
            }
            track.write(buffer, 0, buffer.size)
        }
    } finally {
        try {
            track.stop()
            track.release()
        } catch (_: Exception) {}
    }
}

private suspend fun playSweepWorker(
    onFreqUpdate: (Float) -> Unit,
    onComplete: () -> Unit
) {
    val sampleRate = 44100
    val durationSeconds = 5.0
    val minFreq = 20.0
    val maxFreq = 20000.0

    val minBufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    val bufferSize = (minBufferSize * 2).coerceAtLeast(sampleRate / 4)

    val track = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
        .setBufferSizeInBytes(bufferSize)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

    try {
        track.play()
        val totalSamples = (sampleRate * durationSeconds).toInt()
        val chunkSize = bufferSize / 2
        val buffer = ShortArray(chunkSize)
        var sampleIndex = 0
        var phase = 0.0

        while (sampleIndex < totalSamples && kotlinx.coroutines.currentCoroutineContext().isActive) {
            val count = (totalSamples - sampleIndex).coerceAtMost(chunkSize)
            for (i in 0 until count) {
                val t = (sampleIndex + i) / (sampleRate * durationSeconds)
                // Logarithmic frequency sweep
                val freq = minFreq * Math.pow(maxFreq / minFreq, t)
                val phaseInc = 2.0 * PI * freq / sampleRate
                buffer[i] = (sin(phase) * 32000).toInt().toShort()
                phase += phaseInc
                if (phase > 2.0 * PI) phase -= 2.0 * PI
            }
            track.write(buffer, 0, count)
            val currentFreq = minFreq * Math.pow(maxFreq / minFreq, sampleIndex / (sampleRate * durationSeconds))
            onFreqUpdate(currentFreq.toFloat())
            sampleIndex += count
        }
    } finally {
        try {
            track.stop()
            track.release()
        } catch (_: Exception) {}
        onComplete()
    }
}

@Composable
private fun StereoChannelSection(context: Context) {
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
    var currentVolume by remember { mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) }

    var playingChannel by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var channelJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            channelJob?.cancel()
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.audio_stereo_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.audio_stereo_desc),
                style = MaterialTheme.typography.bodyMedium
            )

            com.example.ui.components.M3SliderWithIcons(
                value = currentVolume.toFloat(),
                onValueChange = {
                    val vol = it.toInt()
                    currentVolume = vol
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0)
                },
                valueRange = 0f..maxVolume.toFloat(),
                steps = if (maxVolume > 1) maxVolume - 1 else 0,
                startIcon = Icons.Default.VolumeMute,
                endIcon = Icons.Default.VolumeUp,
                valueLabel = stringResource(R.string.audio_volume_label, currentVolume, maxVolume),
                testTag = "volume_slider"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (playingChannel == "LEFT") {
                            channelJob?.cancel()
                            playingChannel = null
                        } else {
                            channelJob?.cancel()
                            playingChannel = "LEFT"
                            channelJob = scope.launch(Dispatchers.Default) {
                                playStereoTone(pan = -1.0f)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("play_left_channel"),
                    colors = if (playingChannel == "LEFT") ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                             else ButtonDefaults.buttonColors(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.audio_play_left))
                }

                Button(
                    onClick = {
                        if (playingChannel == "RIGHT") {
                            channelJob?.cancel()
                            playingChannel = null
                        } else {
                            channelJob?.cancel()
                            playingChannel = "RIGHT"
                            channelJob = scope.launch(Dispatchers.Default) {
                                playStereoTone(pan = 1.0f)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("play_right_channel"),
                    colors = if (playingChannel == "RIGHT") ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                             else ButtonDefaults.buttonColors(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.audio_play_right))
                }
            }

            OutlinedButton(
                onClick = {
                    if (playingChannel == "BOTH") {
                        channelJob?.cancel()
                        playingChannel = null
                    } else {
                        channelJob?.cancel()
                        playingChannel = "BOTH"
                        channelJob = scope.launch(Dispatchers.Default) {
                            playStereoTone(pan = 0.0f)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("play_stereo_both"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.audio_play_both))
            }
        }
    }
}

private suspend fun playStereoTone(pan: Float) {
    val sampleRate = 44100
    val freq = 1000.0
    val minBufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_STEREO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    val bufferSize = (minBufferSize * 2).coerceAtLeast(sampleRate / 4)

    val track = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build()
        )
        .setBufferSizeInBytes(bufferSize)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

    try {
        track.play()
        val buffer = ShortArray(bufferSize / 2)
        var phase = 0.0
        val phaseInc = 2.0 * PI * freq / sampleRate
        val leftGain = if (pan <= 0) 1.0f else (1.0f - pan)
        val rightGain = if (pan >= 0) 1.0f else (1.0f + pan)

        while (kotlinx.coroutines.currentCoroutineContext().isActive) {
            var i = 0
            while (i < buffer.size) {
                val sample = (sin(phase) * 32000).toInt()
                buffer[i] = (sample * leftGain).toInt().toShort()      // Left channel
                buffer[i + 1] = (sample * rightGain).toInt().toShort()  // Right channel
                phase += phaseInc
                if (phase > 2.0 * PI) phase -= 2.0 * PI
                i += 2
            }
            track.write(buffer, 0, buffer.size)
        }
    } finally {
        try {
            track.stop()
            track.release()
        } catch (_: Exception) {}
    }
}

@Composable
private fun MicrophoneTestSection(context: Context) {
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    var isRecording by remember { mutableStateOf(false) }
    var recordingSecondsRemaining by remember { mutableIntStateOf(5) }
    var recordedAudioBytes by remember { mutableStateOf<ByteArray?>(null) }
    var isPlayingBack by remember { mutableStateOf(false) }
    var micLevelFraction by remember { mutableFloatStateOf(0f) }
    val animatedLevel by animateFloatAsState(targetValue = micLevelFraction, label = "mic_level")

    val scope = rememberCoroutineScope()
    var activeJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            activeJob?.cancel()
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.audio_mic_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.audio_mic_desc),
                style = MaterialTheme.typography.bodyMedium
            )

            if (!hasPermission) {
                Text(
                    text = stringResource(R.string.audio_mic_permission_needed),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = { launcher.launch(Manifest.permission.RECORD_AUDIO) },
                    modifier = Modifier.fillMaxWidth().testTag("grant_mic_permission"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.grant_permission))
                }
            } else {
                Text(
                    text = stringResource(R.string.audio_mic_level),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = { animatedLevel },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp)),
                    color = if (animatedLevel > 0.8f) Color(0xFFE53935) else Color(0xFF00C853),
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )

                if (isRecording) {
                    Text(
                        text = stringResource(R.string.audio_recording_progress, recordingSecondsRemaining),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (recordedAudioBytes != null) {
                    Text(
                        text = stringResource(R.string.audio_recording_done),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (!isRecording) {
                                isRecording = true
                                recordingSecondsRemaining = 5
                                activeJob = scope.launch(Dispatchers.Default) {
                                    val sampleRate = 44100
                                    val bufferSize = AudioRecord.getMinBufferSize(
                                        sampleRate,
                                        AudioFormat.CHANNEL_IN_MONO,
                                        AudioFormat.ENCODING_PCM_16BIT
                                    )
                                    val recorder = AudioRecord(
                                        MediaRecorder.AudioSource.MIC,
                                        sampleRate,
                                        AudioFormat.CHANNEL_IN_MONO,
                                        AudioFormat.ENCODING_PCM_16BIT,
                                        bufferSize
                                    )
                                    val baos = ByteArrayOutputStream()
                                    val audioBuffer = ShortArray(bufferSize / 2)
                                    try {
                                        recorder.startRecording()
                                        val startTime = System.currentTimeMillis()
                                        while (System.currentTimeMillis() - startTime < 5000 && isActive) {
                                            val read = recorder.read(audioBuffer, 0, audioBuffer.size)
                                            if (read > 0) {
                                                var maxAmp = 0
                                                for (i in 0 until read) {
                                                    val amp = Math.abs(audioBuffer[i].toInt())
                                                    if (amp > maxAmp) maxAmp = amp
                                                    baos.write(audioBuffer[i].toInt() and 0xFF)
                                                    baos.write((audioBuffer[i].toInt() shr 8) and 0xFF)
                                                }
                                                val fraction = (maxAmp / 32767f).coerceIn(0f, 1f)
                                                withContext(Dispatchers.Main) {
                                                    micLevelFraction = fraction
                                                    val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                                                    recordingSecondsRemaining = (5 - elapsedSec).coerceAtLeast(0)
                                                }
                                            }
                                        }
                                    } finally {
                                        try {
                                            recorder.stop()
                                            recorder.release()
                                        } catch (_: Exception) {}
                                        withContext(Dispatchers.Main) {
                                            recordedAudioBytes = baos.toByteArray()
                                            isRecording = false
                                            micLevelFraction = 0f
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isRecording && !isPlayingBack,
                        modifier = Modifier.weight(1f).testTag("record_mic_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.audio_record_5s))
                    }

                    OutlinedButton(
                        onClick = {
                            val bytes = recordedAudioBytes ?: return@OutlinedButton
                            isPlayingBack = true
                            activeJob = scope.launch(Dispatchers.Default) {
                                val sampleRate = 44100
                                val track = AudioTrack.Builder()
                                    .setAudioAttributes(
                                        AudioAttributes.Builder()
                                            .setUsage(AudioAttributes.USAGE_MEDIA)
                                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                            .build()
                                    )
                                    .setAudioFormat(
                                        AudioFormat.Builder()
                                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                            .setSampleRate(sampleRate)
                                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                            .build()
                                    )
                                    .setBufferSizeInBytes(bytes.size)
                                    .setTransferMode(AudioTrack.MODE_STATIC)
                                    .build()

                                try {
                                    track.write(bytes, 0, bytes.size)
                                    track.play()
                                    delay((bytes.size.toDouble() / (sampleRate * 2) * 1000).toLong() + 500)
                                } finally {
                                    try {
                                        track.stop()
                                        track.release()
                                    } catch (_: Exception) {}
                                    withContext(Dispatchers.Main) {
                                        isPlayingBack = false
                                    }
                                }
                            }
                        },
                        enabled = recordedAudioBytes != null && !isRecording && !isPlayingBack,
                        modifier = Modifier.weight(1f).testTag("playback_mic_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.audio_play_recording))
                    }
                }
            }
        }
    }
}
