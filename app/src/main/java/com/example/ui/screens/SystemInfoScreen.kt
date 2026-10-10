package com.example.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Environment
import android.os.StatFs
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SdStorage
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.TestRepository
import com.example.model.TestCategory
import com.example.model.TestStatus
import com.example.ui.components.InfoRow
import com.example.ui.components.TestControlBar
import java.io.BufferedReader
import java.io.FileReader
import java.util.Locale
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemInfoScreen(
    onNavigateBack: () -> Unit,
    repository: TestRepository = TestRepository.instance
) {
    val context = LocalContext.current
    val testResults by repository.testResults.collectAsState()
    val currentStatus = testResults[TestCategory.SYSTEM] ?: TestStatus.UNTESTED

    // RAM Info
    val actManager = remember { context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager }
    val memInfo = remember {
        ActivityManager.MemoryInfo().also {
            actManager?.getMemoryInfo(it)
        }
    }
    val totalRamGb = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
    val availRamGb = memInfo.availMem / (1024.0 * 1024.0 * 1024.0)
    val ramThresholdGb = memInfo.threshold / (1024.0 * 1024.0 * 1024.0)

    // Storage Info
    val statFs = remember { StatFs(Environment.getDataDirectory().path) }
    val totalStorageGb = (statFs.blockCountLong * statFs.blockSizeLong) / (1024.0 * 1024.0 * 1024.0)
    val freeStorageGb = (statFs.availableBlocksLong * statFs.blockSizeLong) / (1024.0 * 1024.0 * 1024.0)

    // Kernel Info
    val kernelVersion = remember {
        try {
            BufferedReader(FileReader("/proc/version")).use { it.readLine() ?: System.getProperty("os.version") }
        } catch (_: Exception) {
            System.getProperty("os.version") ?: "Linux Unknown"
        }
    }

    // GPU Info via hidden GLSurfaceView
    var gpuVendor by remember { mutableStateOf("Querying...") }
    var gpuRenderer by remember { mutableStateOf("Querying...") }
    var glesVersion by remember { mutableStateOf("Querying...") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.category_system)) },
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
                onStatusChange = { newStatus -> repository.setStatus(TestCategory.SYSTEM, newStatus) },
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
            // Hidden GLSurfaceView to safely extract real GPU hardware strings
            Box(modifier = Modifier.size(1.dp)) {
                AndroidView(
                    factory = { ctx ->
                        GLSurfaceView(ctx).apply {
                            setRenderer(object : GLSurfaceView.Renderer {
                                override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
                                    val vendor = gl.glGetString(GL10.GL_VENDOR) ?: "Unknown"
                                    val renderer = gl.glGetString(GL10.GL_RENDERER) ?: "Unknown"
                                    val version = gl.glGetString(GL10.GL_VERSION) ?: "Unknown"
                                    post {
                                        gpuVendor = vendor
                                        gpuRenderer = renderer
                                        glesVersion = version
                                    }
                                }
                                override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {}
                                override fun onDrawFrame(gl: GL10) {}
                            })
                        }
                    }
                )
            }

            // Device Identification Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Device Identity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    InfoRow(label = stringResource(R.string.sys_device), value = Build.MODEL)
                    InfoRow(label = stringResource(R.string.sys_manufacturer), value = Build.MANUFACTURER)
                    InfoRow(label = stringResource(R.string.sys_brand), value = "${Build.BRAND} / ${Build.PRODUCT}")
                    InfoRow(label = stringResource(R.string.sys_board), value = "${Build.BOARD} / ${Build.HARDWARE}")
                    InfoRow(label = stringResource(R.string.sys_soc_abi), value = Build.SUPPORTED_ABIS.joinToString(", "))
                }
            }

            // Android OS Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("OS & Firmware", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    InfoRow(label = stringResource(R.string.sys_android_ver), value = "Android ${Build.VERSION.RELEASE}")
                    InfoRow(label = stringResource(R.string.sys_api_level), value = "API ${Build.VERSION.SDK_INT}")
                    InfoRow(label = stringResource(R.string.sys_security_patch), value = Build.VERSION.SECURITY_PATCH)
                    InfoRow(label = stringResource(R.string.sys_build_id), value = Build.ID)
                    InfoRow(label = stringResource(R.string.sys_kernel), value = kernelVersion)
                }
            }

            // Memory & Storage Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Memory & Storage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.SdStorage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    InfoRow(label = stringResource(R.string.sys_ram_total), value = String.format(Locale.US, "%.2f GB", totalRamGb))
                    InfoRow(label = stringResource(R.string.sys_ram_available), value = String.format(Locale.US, "%.2f GB", availRamGb))
                    InfoRow(label = "RAM Low Memory Flag", value = if (memInfo.lowMemory) "Low Memory Alert" else "Normal")
                    InfoRow(label = stringResource(R.string.sys_storage_total), value = String.format(Locale.US, "%.2f GB", totalStorageGb))
                    InfoRow(label = stringResource(R.string.sys_storage_free), value = String.format(Locale.US, "%.2f GB", freeStorageGb))
                }
            }

            // GPU Renderer Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("GPU Graphics Subsystem", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.DeveloperBoard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    InfoRow(label = stringResource(R.string.sys_gpu_vendor), value = gpuVendor)
                    InfoRow(label = stringResource(R.string.sys_gpu_renderer), value = gpuRenderer)
                    InfoRow(label = stringResource(R.string.sys_gles_version), value = glesVersion)
                }
            }
        }
    }
}
