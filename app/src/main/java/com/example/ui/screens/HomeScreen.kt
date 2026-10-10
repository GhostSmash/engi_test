package com.example.ui.screens

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppThemeMode
import com.example.data.AppViewMode
import com.example.data.PreferencesManager
import com.example.data.TestRepository
import com.example.model.TestCategory
import com.example.model.TestStatus
import com.example.ui.components.StatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    preferencesManager: PreferencesManager,
    onNavigateToCategory: (TestCategory) -> Unit,
    onOpenPreferences: () -> Unit,
    onNavigateBack: () -> Unit,
    repository: TestRepository = TestRepository.instance
) {
    androidx.activity.compose.BackHandler {
        onNavigateBack()
    }
    val context = LocalContext.current
    val testResults by repository.testResults.collectAsState()
    val themeMode by preferencesManager.themeMode.collectAsState()
    val viewMode by preferencesManager.viewMode.collectAsState()
    val currentLangTag by preferencesManager.currentLanguageTag.collectAsState()

    var showThemeMenu by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf<TestStatus?>(null) }

    val passedCount = testResults.count { it.value == TestStatus.PASSED }
    val failedCount = testResults.count { it.value == TestStatus.FAILED }
    val untestedCount = testResults.count { it.value == TestStatus.UNTESTED }
    val totalCount = TestCategory.values().size

    val filteredCategories = remember(selectedFilter, testResults) {
        if (selectedFilter == null) {
            TestCategory.values().toList()
        } else {
            TestCategory.values().filter { (testResults[it] ?: TestStatus.UNTESTED) == selectedFilter }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.reset_all_tests)) },
            text = { Text(stringResource(R.string.reset_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.resetAll()
                        showResetDialog = false
                    }
                ) {
                    Text(stringResource(R.string.reset), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.mode_manual_title),
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
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("nav_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    // View Mode Switcher (Grid / List)
                    IconButton(
                        onClick = {
                            val nextMode = if (viewMode == AppViewMode.GRID) AppViewMode.LIST else AppViewMode.GRID
                            preferencesManager.setViewMode(nextMode)
                        },
                        modifier = Modifier.testTag("action_toggle_view_mode")
                    ) {
                        Icon(
                            imageVector = if (viewMode == AppViewMode.GRID) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = stringResource(if (viewMode == AppViewMode.GRID) R.string.view_mode_list else R.string.view_mode_grid)
                        )
                    }

                    // Theme Mode Switcher
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

                    // Language Switcher
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

                    // Export Report
                    IconButton(
                        onClick = { repository.shareReport(context) },
                        modifier = Modifier.testTag("action_export_report")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.export_report))
                    }

                    // More Menu
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings_title)) },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    onOpenPreferences()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.reset_all_tests)) },
                                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    showResetDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
                // Minimalist Telemetry Overview Banner
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .surfaceIndicator(passedCount, failedCount, totalCount)
                            )
                            Text(
                                text = "DIAGNOSTIC STATUS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val percent = if (totalCount > 0) (passedCount * 100) / totalCount else 0
                        Text(
                            text = "$passedCount/$totalCount ($percent%)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { if (totalCount > 0) passedCount.toFloat() / totalCount else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF10B981),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    // Filter Chips Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == null,
                            onClick = { selectedFilter = null },
                            label = { Text("All ($totalCount)") },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        )
                        FilterChip(
                            selected = selectedFilter == TestStatus.PASSED,
                            onClick = { selectedFilter = if (selectedFilter == TestStatus.PASSED) null else TestStatus.PASSED },
                            label = { Text("Pass ($passedCount)") },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        )
                        FilterChip(
                            selected = selectedFilter == TestStatus.FAILED,
                            onClick = { selectedFilter = if (selectedFilter == TestStatus.FAILED) null else TestStatus.FAILED },
                            label = { Text("Fail ($failedCount)") },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        )
                        FilterChip(
                            selected = selectedFilter == TestStatus.UNTESTED,
                            onClick = { selectedFilter = if (selectedFilter == TestStatus.UNTESTED) null else TestStatus.UNTESTED },
                            label = { Text("Untested ($untestedCount)") },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        )
                    }
                }
            }

            // Categories layout: Grid vs List
            if (viewMode == AppViewMode.GRID) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredCategories) { category ->
                        val status = testResults[category] ?: TestStatus.UNTESTED
                        MinimalGridCard(
                            category = category,
                            status = status,
                            onClick = { onNavigateToCategory(category) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCategories) { category ->
                        val status = testResults[category] ?: TestStatus.UNTESTED
                        MinimalListCard(
                            category = category,
                            status = status,
                            onClick = { onNavigateToCategory(category) }
                        )
                    }
                }
            }
        }
    }
}
}

private fun Modifier.surfaceIndicator(passed: Int, failed: Int, total: Int): Modifier {
    val color = when {
        failed > 0 -> Color(0xFFEF4444)
        passed == total -> Color(0xFF10B981)
        passed > 0 -> Color(0xFF3B82F6)
        else -> Color(0xFF9CA3AF)
    }
    return this.then(Modifier.clip(CircleShape)).then(Modifier.size(8.dp))
}

@Composable
private fun MinimalGridCard(
    category: TestCategory,
    status: TestStatus,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("grid_card_${category.route}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                StatusChip(status = status)
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(category.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = stringResource(category.descriptionRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun MinimalListCard(
    category: TestCategory,
    status: TestStatus,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("list_card_${category.route}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(category.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    StatusChip(status = status)
                }

                Text(
                    text = stringResource(category.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
