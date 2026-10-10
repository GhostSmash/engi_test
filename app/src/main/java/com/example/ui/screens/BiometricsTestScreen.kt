package com.example.ui.screens

import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.ui.components.InfoRow
import com.example.ui.components.TestControlBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricsTestScreen(
    onNavigateBack: () -> Unit,
    repository: TestRepository = TestRepository.instance
) {
    val context = LocalContext.current
    val testResults by repository.testResults.collectAsState()
    val currentStatus = testResults[TestCategory.BIOMETRICS] ?: TestStatus.UNTESTED

    val biometricManager = remember { BiometricManager.from(context) }
    val strongResult = remember {
        biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
    }
    val weakResult = remember {
        biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
    }

    val strongStatusStr = when (strongResult) {
        BiometricManager.BIOMETRIC_SUCCESS -> "Class 3 (Strong Biometrics Supported & Enrolled)"
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Hardware Present (No Biometrics Enrolled)"
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No Biometric Hardware"
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Hardware Currently Unavailable"
        else -> "Unknown Status"
    }

    val weakStatusStr = when (weakResult) {
        BiometricManager.BIOMETRIC_SUCCESS -> "Class 2 (Weak Biometrics Supported & Enrolled)"
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Hardware Present (No Biometrics Enrolled)"
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No Biometric Hardware"
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Hardware Currently Unavailable"
        else -> "Unknown Status"
    }

    var authResultText by remember { mutableStateOf<String?>(null) }
    var isAuthSuccess by remember { mutableStateOf<Boolean?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.category_biometrics)) },
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
                onStatusChange = { newStatus -> repository.setStatus(TestCategory.BIOMETRICS, newStatus) },
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
            // Hardware Status Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.biometric_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }

                    InfoRow(label = "Class 3 (Strong)", value = strongStatusStr)
                    InfoRow(label = "Class 2 (Weak)", value = weakStatusStr)
                }
            }

            // Interactive Prompt Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Biometric Prompt Verification",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Test the system biometric dialog directly to verify sensor responsiveness.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            val activity = context as? AppCompatActivity
                            if (activity == null) {
                                authResultText = "Activity does not support Fragment-based BiometricPrompt"
                                isAuthSuccess = false
                                return@Button
                            }

                            val executor = ContextCompat.getMainExecutor(activity)
                            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                .setTitle(activity.getString(R.string.biometric_prompt_title))
                                .setSubtitle(activity.getString(R.string.biometric_prompt_subtitle))
                                .setNegativeButtonText(activity.getString(R.string.cancel))
                                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
                                .build()

                            val biometricPrompt = BiometricPrompt(
                                activity,
                                executor,
                                object : BiometricPrompt.AuthenticationCallback() {
                                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                        super.onAuthenticationSucceeded(result)
                                        authResultText = activity.getString(R.string.biometric_auth_success)
                                        isAuthSuccess = true
                                    }

                                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                        super.onAuthenticationError(errorCode, errString)
                                        authResultText = activity.getString(R.string.biometric_auth_error, errString)
                                        isAuthSuccess = false
                                    }

                                    override fun onAuthenticationFailed() {
                                        super.onAuthenticationFailed()
                                        authResultText = activity.getString(R.string.biometric_auth_failed)
                                        isAuthSuccess = false
                                    }
                                }
                            )

                            try {
                                biometricPrompt.authenticate(promptInfo)
                            } catch (e: Exception) {
                                authResultText = "Failed to launch: ${e.message}"
                                isAuthSuccess = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("launch_biometric_prompt"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.biometric_test_button))
                    }

                    if (authResultText != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isAuthSuccess == true) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00C853))
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                text = authResultText ?: "",
                                color = if (isAuthSuccess == true) Color(0xFF00C853) else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
