package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.TestRepository
import com.example.model.TestCategory
import com.example.model.TestStatus
import com.example.ui.components.FullscreenMode
import com.example.ui.components.InfoRow
import com.example.ui.components.TestControlBar
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayTestScreen(
    onNavigateBack: () -> Unit,
    repository: TestRepository = TestRepository.instance
) {
    val testResults by repository.testResults.collectAsState()
    val currentStatus = testResults[TestCategory.DISPLAY] ?: TestStatus.UNTESTED

    var selectedTab by remember { mutableIntStateOf(0) }
    var activeFullscreenMode by remember { mutableStateOf<String?>(null) }

    if (activeFullscreenMode != null) {
        FullscreenMode(enabled = true)
        BackHandler {
            activeFullscreenMode = null
        }
        when (activeFullscreenMode) {
            "COLORS" -> FullscreenSolidColors(onExit = { activeFullscreenMode = null })
            "GRADIENTS" -> FullscreenGradients(onExit = { activeFullscreenMode = null })
            "TOUCH_GRID" -> FullscreenTouchGrid(onExit = { activeFullscreenMode = null })
            "MULTI_TOUCH" -> FullscreenMultiTouch(onExit = { activeFullscreenMode = null })
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.category_display)) },
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
                onStatusChange = { newStatus -> repository.setStatus(TestCategory.DISPLAY, newStatus) },
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
                androidx.compose.material3.ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(stringResource(R.string.display_colors_title)) },
                        icon = { Icon(Icons.Default.ColorLens, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(stringResource(R.string.display_gradients_title)) },
                        icon = { Icon(Icons.Default.LinearScale, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text(stringResource(R.string.display_touch_grid_title)) },
                        icon = { Icon(Icons.Default.GridOn, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text(stringResource(R.string.display_multitouch_title)) },
                        icon = { Icon(Icons.Default.TouchApp, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = { Text(stringResource(R.string.display_info_title)) },
                        icon = { Icon(Icons.Default.Info, contentDescription = null) }
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
                    0 -> {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = stringResource(R.string.display_colors_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.display_colors_desc),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = stringResource(R.string.display_colors_tap_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = { activeFullscreenMode = "COLORS" },
                                    modifier = Modifier.fillMaxWidth().testTag("start_colors_test"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Fullscreen, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.action_start))
                                }
                            }
                        }
                    }
                    1 -> {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = stringResource(R.string.display_gradients_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.display_gradients_desc),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Button(
                                    onClick = { activeFullscreenMode = "GRADIENTS" },
                                    modifier = Modifier.fillMaxWidth().testTag("start_gradients_test"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Fullscreen, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.action_start))
                                }
                            }
                        }
                    }
                    2 -> {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = stringResource(R.string.display_touch_grid_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.display_touch_grid_desc),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = stringResource(R.string.touch_grid_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = { activeFullscreenMode = "TOUCH_GRID" },
                                    modifier = Modifier.fillMaxWidth().testTag("start_touch_grid_test"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Fullscreen, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.action_start))
                                }
                            }
                        }
                    }
                    3 -> {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = stringResource(R.string.display_multitouch_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.display_multitouch_desc),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = stringResource(R.string.multitouch_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = { activeFullscreenMode = "MULTI_TOUCH" },
                                    modifier = Modifier.fillMaxWidth().testTag("start_multitouch_test"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Fullscreen, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.action_start))
                                }
                            }
                        }
                    }
                    4 -> {
                        DisplayInfoAndBrightnessSection()
                    }
                }
            }
        }
    }
}
}

@Composable
private fun FullscreenSolidColors(onExit: () -> Unit) {
    val colors = listOf(
        Color.Red,
        Color.Green,
        Color.Blue,
        Color.White,
        Color.Black
    )
    val colorNames = listOf(
        R.string.display_color_red,
        R.string.display_color_green,
        R.string.display_color_blue,
        R.string.display_color_white,
        R.string.display_color_black
    )
    var currentIndex by remember { mutableIntStateOf(0) }
    var showHint by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors[currentIndex])
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onExit() },
                    onTap = {
                        currentIndex = (currentIndex + 1) % colors.size
                        showHint = false
                    }
                )
            }
    ) {
        if (showHint) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(colorNames[currentIndex]),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.display_colors_tap_hint),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.double_tap_to_exit),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun FullscreenGradients(onExit: () -> Unit) {
    val modes = listOf(
        R.string.gradient_grayscale,
        R.string.gradient_red,
        R.string.gradient_green,
        R.string.gradient_blue
    )
    var currentMode by remember { mutableIntStateOf(0) }

    val brush = when (currentMode) {
        0 -> Brush.verticalGradient(listOf(Color.Black, Color.White))
        1 -> Brush.verticalGradient(listOf(Color.Black, Color.Red))
        2 -> Brush.verticalGradient(listOf(Color.Black, Color.Green))
        else -> Brush.verticalGradient(listOf(Color.Black, Color.Blue))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onExit() },
                    onTap = { currentMode = (currentMode + 1) % modes.size }
                )
            }
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp, start = 20.dp, end = 20.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.65f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(modes[currentMode]),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.double_tap_to_exit),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun FullscreenTouchGrid(onExit: () -> Unit) {
    // Screen is divided into rows and columns plus diagonals
    val cols = 10
    val rows = 18
    val coveredCells = remember { mutableStateMapOf<Int, Boolean>() }
    val totalCells = remember {
        // We track perimeter cells (edges & corners) and two diagonal line cells
        val set = mutableSetOf<Int>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val isEdge = (r == 0 || r == rows - 1 || c == 0 || c == cols - 1)
                val isDiag1 = ((r.toFloat() / rows - c.toFloat() / cols).let { kotlin.math.abs(it) < 0.12f })
                val isDiag2 = (((rows - 1 - r).toFloat() / rows - c.toFloat() / cols).let { kotlin.math.abs(it) < 0.12f })
                if (isEdge || isDiag1 || isDiag2) {
                    set.add(r * cols + c)
                }
            }
        }
        set
    }

    val coveredCount = totalCells.count { coveredCells[it] == true }
    val isComplete = coveredCount == totalCells.size

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { onExit() })
            }
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val cellW = widthPx / cols
        val cellH = heightPx / rows

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            for (change in event.changes) {
                                val pos = change.position
                                val c = (pos.x / cellW).toInt().coerceIn(0, cols - 1)
                                val r = (pos.y / cellH).toInt().coerceIn(0, rows - 1)
                                val idx = r * cols + c
                                if (totalCells.contains(idx)) {
                                    coveredCells[idx] = true
                                }
                            }
                        }
                    }
                }
        ) {
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val idx = r * cols + c
                    if (totalCells.contains(idx)) {
                        val isCovered = coveredCells[idx] == true
                        val color = if (isCovered) Color(0xFF00E676) else Color(0xFF333333)
                        drawRect(
                            color = color,
                            topLeft = Offset(c * cellW, r * cellH),
                            size = Size(cellW, cellH)
                        )
                        drawRect(
                            color = Color(0xFF1E1E1E),
                            topLeft = Offset(c * cellW, r * cellH),
                            size = Size(cellW, cellH),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isComplete) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(36.dp))
                    Text(
                        text = stringResource(R.string.touch_grid_completed),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                } else {
                    val pct = if (totalCells.isNotEmpty()) (coveredCount * 100) / totalCells.size else 0
                    Text(
                        text = stringResource(R.string.touch_grid_coverage, coveredCount, totalCells.size, pct),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.touch_grid_hint),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = stringResource(R.string.double_tap_to_exit),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

private data class TouchPointer(val id: Int, val x: Float, val y: Float)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FullscreenMultiTouch(onExit: () -> Unit) {
    val pointers = remember { mutableStateListOf<TouchPointer>() }
    var maxPointers by remember { mutableIntStateOf(0) }

    val colors = listOf(
        Color(0xFFFF5252), Color(0xFFFF4081), Color(0xFFE040FB),
        Color(0xFF7C4DFF), Color(0xFF536DFE), Color(0xFF448AFF),
        Color(0xFF40C4FF), Color(0xFF18FFFF), Color(0xFF64FFDA),
        Color(0xFF69F0AE)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { onExit() })
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter { motionEvent ->
                    when (motionEvent.actionMasked) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN,
                        MotionEvent.ACTION_MOVE -> {
                            pointers.clear()
                            val count = motionEvent.pointerCount
                            if (count > maxPointers) {
                                maxPointers = count
                            }
                            for (i in 0 until count) {
                                pointers.add(
                                    TouchPointer(
                                        id = motionEvent.getPointerId(i),
                                        x = motionEvent.getX(i),
                                        y = motionEvent.getY(i)
                                    )
                                )
                            }
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP,
                        MotionEvent.ACTION_CANCEL -> {
                            pointers.clear()
                            val count = motionEvent.pointerCount
                            for (i in 0 until count) {
                                if (i != motionEvent.actionIndex) {
                                    pointers.add(
                                        TouchPointer(
                                            id = motionEvent.getPointerId(i),
                                            x = motionEvent.getX(i),
                                            y = motionEvent.getY(i)
                                        )
                                    )
                                }
                            }
                        }
                    }
                    true
                }
        ) {
            for (p in pointers) {
                val color = colors[p.id % colors.size]
                drawCircle(
                    color = color.copy(alpha = 0.25f),
                    radius = 90.dp.toPx(),
                    center = Offset(p.x, p.y)
                )
                drawCircle(
                    color = color,
                    radius = 45.dp.toPx(),
                    center = Offset(p.x, p.y),
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = Offset(p.x, p.y)
                )
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.multitouch_current, pointers.size),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = stringResource(R.string.multitouch_max, maxPointers),
                    color = Color(0xFF00E676),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.double_tap_to_exit),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun DisplayInfoAndBrightnessSection() {
    val context = LocalContext.current
    val window = (context as? Activity)?.window
    val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.display
    } else {
        @Suppress("DEPRECATION")
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
    }

    val metrics = context.resources.displayMetrics
    val width = metrics.widthPixels
    val height = metrics.heightPixels
    val densityDpi = metrics.densityDpi
    val refreshRate = display?.refreshRate ?: 60f

    val supportedModesStr = display?.supportedModes?.joinToString(", ") { mode ->
        "${mode.physicalWidth}x${mode.physicalHeight} @ ${mode.refreshRate.roundToInt()}Hz"
    } ?: stringResource(R.string.not_available)

    val hdrSupported = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val hdr = display?.isHdr == true
        if (hdr) stringResource(R.string.yes) else stringResource(R.string.no)
    } else {
        stringResource(R.string.not_available)
    }

    val wideColorGamut = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val wide = display?.isWideColorGamut == true
        if (wide) stringResource(R.string.yes) else stringResource(R.string.no)
    } else {
        stringResource(R.string.not_available)
    }

    var brightnessSlider by remember { mutableFloatStateOf(0.7f) }

    DisposableEffect(brightnessSlider) {
        window?.let { win ->
            val layoutParams = win.attributes
            layoutParams.screenBrightness = brightnessSlider
            win.attributes = layoutParams
        }
        onDispose {
            window?.let { win ->
                val layoutParams = win.attributes
                layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                win.attributes = layoutParams
            }
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.display_info_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            InfoRow(label = stringResource(R.string.screen_resolution), value = "${width} x ${height} px")
            InfoRow(label = stringResource(R.string.screen_density), value = "$densityDpi dpi (${metrics.density}x)")
            InfoRow(label = stringResource(R.string.screen_refresh_rate), value = String.format(java.util.Locale.US, "%.1f Hz", refreshRate))
            InfoRow(label = stringResource(R.string.screen_hdr_support), value = hdrSupported)
            InfoRow(label = stringResource(R.string.screen_wide_color), value = wideColorGamut)
            InfoRow(label = stringResource(R.string.screen_supported_modes), value = supportedModesStr)

            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.screen_brightness_test),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            com.example.ui.components.M3SliderWithIcons(
                value = brightnessSlider,
                onValueChange = { brightnessSlider = it },
                valueRange = 0.05f..1.0f,
                startIcon = Icons.Default.LinearScale,
                endIcon = Icons.Default.ColorLens,
                valueLabel = "${(brightnessSlider * 100).toInt()}%",
                testTag = "brightness_slider"
            )
        }
    }
}
