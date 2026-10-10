package com.example.ui.screens

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppThemeMode
import com.example.data.PreferencesManager
import com.example.data.TestRepository
import com.example.model.TestCategory
import com.example.model.TestStatus
import com.example.ui.components.AmbientBackground
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectScreen(
    preferencesManager: PreferencesManager,
    onNavigateToBasic: () -> Unit,
    onNavigateToFull: () -> Unit,
    onNavigateToManual: () -> Unit,
    onOpenPreferences: () -> Unit,
    repository: TestRepository = TestRepository.instance
) {
    val context = LocalContext.current
    val testResults by repository.testResults.collectAsState()
    val themeMode by preferencesManager.themeMode.collectAsState()
    val currentLangTag by preferencesManager.currentLanguageTag.collectAsState()

    var showThemeMenu by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }

    val passedCount = testResults.count { it.value == TestStatus.PASSED }
    val failedCount = testResults.count { it.value == TestStatus.FAILED }
    val totalCount = TestCategory.values().size
    val progress = if (totalCount > 0) passedCount.toFloat() / totalCount else 0f

    // Animated entrance transitions
    var visibleCards by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(60)
        visibleCards = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "${Build.MANUFACTURER} ${Build.MODEL}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Quick Theme Mode Switcher
                    Box {
                        IconButton(
                            onClick = { showThemeMenu = true },
                            modifier = Modifier.testTag("action_theme_switcher")
                        ) {
                            Icon(
                                imageVector = when (themeMode) {
                                    AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                    AppThemeMode.LIGHT -> Icons.Default.LightMode
                                    AppThemeMode.DARK -> Icons.Default.DarkMode
                                },
                                contentDescription = stringResource(R.string.theme_switcher)
                            )
                        }
                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.theme_system)) },
                                leadingIcon = { Icon(Icons.Default.BrightnessAuto, contentDescription = null) },
                                onClick = {
                                    showThemeMenu = false
                                    preferencesManager.setThemeMode(AppThemeMode.SYSTEM)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.theme_light)) },
                                leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null) },
                                onClick = {
                                    showThemeMenu = false
                                    preferencesManager.setThemeMode(AppThemeMode.LIGHT)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.theme_dark)) },
                                leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                                onClick = {
                                    showThemeMenu = false
                                    preferencesManager.setThemeMode(AppThemeMode.DARK)
                                }
                            )
                        }
                    }

                    // Quick Language Switcher
                    Box {
                        IconButton(
                            onClick = { showLanguageMenu = true },
                            modifier = Modifier.testTag("action_language")
                        ) {
                            Icon(Icons.Default.Language, contentDescription = stringResource(R.string.language_switcher))
                        }
                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.lang_system)) },
                                onClick = {
                                    showLanguageMenu = false
                                    preferencesManager.setLanguage("system")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.lang_english)) },
                                onClick = {
                                    showLanguageMenu = false
                                    preferencesManager.setLanguage("en")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.lang_russian)) },
                                onClick = {
                                    showLanguageMenu = false
                                    preferencesManager.setLanguage("ru")
                                }
                            )
                        }
                    }

                    // Start Settings / Preferences
                    IconButton(
                        onClick = onOpenPreferences,
                        modifier = Modifier.testTag("action_open_settings")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title))
                    }

                    // Export Report
                    IconButton(
                        onClick = { repository.shareReport(context) },
                        modifier = Modifier.testTag("action_export_report")
                    ) {
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Telemetry Overview Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)),
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
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = stringResource(R.string.mode_select_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.mode_select_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = "$passedCount / $totalCount",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF10B981),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                // Mode 1: Basic Auto Test (Базовое тестирование)
                AnimatedVisibility(
                    visible = visibleCards,
                    enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                            slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it / 3 }
                ) {
                    ModeCard(
                        badge = if (currentLangTag == "ru") "ЭКСПРЕСС-ПРОВЕРКА 12 ТЕСТОВ" else "EXPRESS 12-POINT AUTO",
                        badgeColor = Color(0xFF0284C7),
                        icon = Icons.Default.Bolt,
                        iconBg = Color(0xFF0284C7).copy(alpha = 0.15f),
                        iconTint = Color(0xFF0284C7),
                        title = stringResource(R.string.mode_basic_title),
                        description = stringResource(R.string.mode_basic_desc),
                        onClick = onNavigateToBasic,
                        testTag = "mode_card_basic"
                    )
                }

                // Mode 2: Full Guided Sequential Test (Сложное тестирование)
                AnimatedVisibility(
                    visible = visibleCards,
                    enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                            slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it / 2 }
                ) {
                    ModeCard(
                        badge = if (currentLangTag == "ru") "ПОЛНЫЙ ПОШАГОВЫЙ АУДИТ" else "12-STEP GUIDED AUDIT",
                        badgeColor = Color(0xFF10B981),
                        icon = Icons.Default.Checklist,
                        iconBg = Color(0xFF10B981).copy(alpha = 0.15f),
                        iconTint = Color(0xFF10B981),
                        title = stringResource(R.string.mode_full_title),
                        description = stringResource(R.string.mode_full_desc),
                        onClick = onNavigateToFull,
                        testTag = "mode_card_full"
                    )
                }

                // Mode 3: Manual Diagnostic Suite (Мануальное тестирование)
                AnimatedVisibility(
                    visible = visibleCards,
                    enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                            slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it }
                ) {
                    ModeCard(
                        badge = if (currentLangTag == "ru") "ИНЖЕНЕРНАЯ ЛАБОРАТОРИЯ" else "ENGINEERING WORKBENCH",
                        badgeColor = Color(0xFF8B5CF6),
                        icon = Icons.Default.DashboardCustomize,
                        iconBg = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                        iconTint = Color(0xFF8B5CF6),
                        title = stringResource(R.string.mode_manual_title),
                        description = stringResource(R.string.mode_manual_desc),
                        onClick = onNavigateToManual,
                        testTag = "mode_card_manual"
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ModeCard(
    badge: String,
    badgeColor: Color,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    description: String,
    onClick: () -> Unit,
    testTag: String
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "mode_card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple()
            ) {
                isPressed = true
                onClick()
            }
            .testTag(testTag),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = iconBg,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.28f))
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = badgeColor,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 21.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.action_start),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
