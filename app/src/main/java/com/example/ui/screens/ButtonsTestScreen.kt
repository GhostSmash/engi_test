package com.example.ui.screens

import android.view.KeyEvent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.KeyEventBus
import com.example.data.TestRepository
import com.example.model.TestCategory
import com.example.model.TestStatus
import com.example.ui.components.InfoRow
import com.example.ui.components.TestControlBar
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonsTestScreen(
    onNavigateBack: () -> Unit,
    repository: TestRepository = TestRepository.instance
) {
    val testResults by repository.testResults.collectAsState()
    val currentStatus = testResults[TestCategory.BUTTONS] ?: TestStatus.UNTESTED

    var volumeUpPresses by remember { mutableIntStateOf(0) }
    var volumeDownPresses by remember { mutableIntStateOf(0) }
    var isVolumeUpActive by remember { mutableStateOf(false) }
    var isVolumeDownActive by remember { mutableStateOf(false) }
    var lastKeyName by remember { mutableStateOf("None") }
    var lastKeyCode by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        KeyEventBus.events.collect { ev ->
            lastKeyName = ev.keyName
            lastKeyCode = ev.keyCode
            when (ev.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    isVolumeUpActive = ev.isDown
                    if (ev.isDown) volumeUpPresses++
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    isVolumeDownActive = ev.isDown
                    if (ev.isDown) volumeDownPresses++
                }
            }
        }
    }

    val volUpCardBg by animateColorAsState(
        targetValue = if (isVolumeUpActive) Color(0xFF00C853)
        else if (volumeUpPresses > 0) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "vol_up_color"
    )

    val volDownCardBg by animateColorAsState(
        targetValue = if (isVolumeDownActive) Color(0xFF00C853)
        else if (volumeDownPresses > 0) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "vol_down_color"
    )

    val bothTested = volumeUpPresses > 0 && volumeDownPresses > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.category_buttons)) },
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
                onStatusChange = { newStatus -> repository.setStatus(TestCategory.BUTTONS, newStatus) },
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
            Text(
                text = stringResource(R.string.buttons_instructions),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Volume Up Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = volUpCardBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.button_volume_up),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.button_press_count, volumeUpPresses),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    if (volumeUpPresses > 0) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00C853), modifier = Modifier.size(28.dp))
                    }
                }
            }

            // Volume Down Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = volDownCardBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeDown, contentDescription = null, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.button_volume_down),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.button_press_count, volumeDownPresses),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    if (volumeDownPresses > 0) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00C853), modifier = Modifier.size(28.dp))
                    }
                }
            }

            // Key Log Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.sensor_live_values),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    InfoRow(label = stringResource(R.string.button_last_detected, lastKeyName), value = if (lastKeyCode > 0) "Code: $lastKeyCode" else "")

                    if (bothTested) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Both volume buttons registered successfully!",
                            color = Color(0xFF00C853),
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.button_test_passed_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
