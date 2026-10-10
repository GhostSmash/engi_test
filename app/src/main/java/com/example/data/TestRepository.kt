package com.example.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.example.R
import com.example.model.TestCategory
import com.example.model.TestStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TestRepository {
    private val _testResults = MutableStateFlow<Map<TestCategory, TestStatus>>(
        TestCategory.values().associateWith { TestStatus.UNTESTED }
    )
    val testResults: StateFlow<Map<TestCategory, TestStatus>> = _testResults.asStateFlow()

    private val _hardwareNotes = MutableStateFlow<Map<String, String>>(emptyMap())
    val hardwareNotes: StateFlow<Map<String, String>> = _hardwareNotes.asStateFlow()

    fun setStatus(category: TestCategory, status: TestStatus) {
        val current = _testResults.value.toMutableMap()
        current[category] = status
        _testResults.value = current
    }

    fun addNote(key: String, value: String) {
        val current = _hardwareNotes.value.toMutableMap()
        current[key] = value
        _hardwareNotes.value = current
    }

    fun resetAll() {
        _testResults.value = TestCategory.values().associateWith { TestStatus.UNTESTED }
        _hardwareNotes.value = emptyMap()
    }

    fun generateReport(context: Context): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val results = _testResults.value
        val passedCount = results.count { it.value == TestStatus.PASSED }
        val failedCount = results.count { it.value == TestStatus.FAILED }
        val skippedCount = results.count { it.value == TestStatus.SKIPPED }
        val untestedCount = results.count { it.value == TestStatus.UNTESTED }

        // Memory info
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val totalRamGb = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
        val availRamGb = memInfo.availMem / (1024.0 * 1024.0 * 1024.0)

        // Storage info
        val stat = StatFs(Environment.getDataDirectory().path)
        val totalStorageGb = (stat.blockCountLong * stat.blockSizeLong) / (1024.0 * 1024.0 * 1024.0)
        val freeStorageGb = (stat.availableBlocksLong * stat.blockSizeLong) / (1024.0 * 1024.0 * 1024.0)

        val sb = StringBuilder()
        sb.append("=========================================\n")
        sb.append("      DEVICE TEST TOOLKIT - DIAGNOSTIC REPORT\n")
        sb.append("=========================================\n")
        sb.append("Timestamp: ").append(dateStr).append("\n\n")

        sb.append("[DEVICE SPECIFICATIONS]\n")
        sb.append("Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n")
        sb.append("Brand / Product: ").append(Build.BRAND).append(" / ").append(Build.PRODUCT).append("\n")
        sb.append("Board / Hardware: ").append(Build.BOARD).append(" / ").append(Build.HARDWARE).append("\n")
        sb.append("Android Version: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n")
        sb.append("Security Patch: ").append(Build.VERSION.SECURITY_PATCH).append("\n")
        sb.append("Build Fingerprint: ").append(Build.FINGERPRINT).append("\n")
        sb.append("CPU ABIs: ").append(Build.SUPPORTED_ABIS.joinToString(", ")).append("\n")
        sb.append(String.format(Locale.US, "RAM: %.2f GB Total / %.2f GB Free\n", totalRamGb, availRamGb))
        sb.append(String.format(Locale.US, "Storage: %.2f GB Total / %.2f GB Free\n", totalStorageGb, freeStorageGb))

        val notes = _hardwareNotes.value
        if (notes.isNotEmpty()) {
            sb.append("\n[HARDWARE TELEMETRY LOGS]\n")
            notes.forEach { (k, v) ->
                sb.append(" • ").append(k).append(": ").append(v).append("\n")
            }
        }

        sb.append("\n[TEST RESULTS SUMMARY]\n")
        sb.append("Total Tests: ").append(TestCategory.values().size).append("\n")
        sb.append("Passed: ").append(passedCount).append("\n")
        sb.append("Failed: ").append(failedCount).append("\n")
        sb.append("Skipped: ").append(skippedCount).append("\n")
        sb.append("Untested: ").append(untestedCount).append("\n\n")

        sb.append("[DETAILED TEST RESULTS]\n")
        TestCategory.values().forEach { cat ->
            val status = results[cat] ?: TestStatus.UNTESTED
            val symbol = when (status) {
                TestStatus.PASSED -> "[PASS] "
                TestStatus.FAILED -> "[FAIL] "
                TestStatus.SKIPPED -> "[SKIP] "
                TestStatus.UNTESTED -> "[----] "
            }
            val title = context.getString(cat.titleRes)
            sb.append(symbol).append(title).append(": ").append(status.name).append("\n")
        }

        sb.append("=========================================\n")
        sb.append("End of Report\n")
        return sb.toString()
    }

    fun shareReport(context: Context) {
        val reportText = generateReport(context)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, reportText)
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.report_title))
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, context.getString(R.string.report_shared_title))
        context.startActivity(shareIntent)
    }

    companion object {
        val instance = TestRepository()
    }
}
