package com.example.ui.screens

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.runtime.DisposableEffect
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
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectivityTestScreen(
    onNavigateBack: () -> Unit,
    repository: TestRepository = TestRepository.instance
) {
    val context = LocalContext.current
    val testResults by repository.testResults.collectAsState()
    val currentStatus = testResults[TestCategory.CONNECTIVITY] ?: TestStatus.UNTESTED

    // Wi-Fi
    val wifiManager = remember { context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager }
    val connManager = remember { context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager }
    val isWifiEnabled = wifiManager?.isWifiEnabled == true
    val activeNetwork = connManager?.activeNetwork
    val caps = activeNetwork?.let { connManager.getNetworkCapabilities(it) }
    val isWifiConnected = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

    var ipAddress by remember {
        mutableStateOf(
            try {
                NetworkInterface.getNetworkInterfaces()?.toList()
                    ?.flatMap { it.inetAddresses.toList() }
                    ?.firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
                    ?.hostAddress ?: "Unavailable"
            } catch (_: Exception) { "Unavailable" }
        )
    }

    // Bluetooth
    @Suppress("DEPRECATION")
    val bluetoothAdapter = remember { BluetoothAdapter.getDefaultAdapter() }
    val isBtSupported = bluetoothAdapter != null
    val isBtEnabled = bluetoothAdapter?.isEnabled == true
    val hasBle = context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

    // NFC
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
    val isNfcSupported = nfcAdapter != null
    val isNfcEnabled = nfcAdapter?.isEnabled == true

    // Cellular
    val telephonyManager = remember { context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager }
    val networkOperatorName = telephonyManager?.networkOperatorName?.ifEmpty { "No SIM / Unknown" } ?: "Unavailable"
    val simStateStr = when (telephonyManager?.simState) {
        TelephonyManager.SIM_STATE_READY -> "SIM Ready"
        TelephonyManager.SIM_STATE_ABSENT -> "No SIM Card"
        TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN Required"
        TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK Required"
        TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Network Locked"
        else -> "Unknown"
    }

    // USB state
    var isUsbConnected by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == "android.hardware.usb.action.USB_STATE") {
                    isUsbConnected = intent.getBooleanExtra("connected", false)
                }
            }
        }
        val filter = IntentFilter("android.hardware.usb.action.USB_STATE")
        context.registerReceiver(receiver, filter)

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    // GPS / Location
    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager }
    val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
    var locationResult by remember { mutableStateOf<String?>(null) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
        if (granted) {
            requestGpsLocation(context, locationManager) { loc ->
                locationResult = if (loc != null) {
                    String.format(Locale.US, "Lat %.4f, Lon %.4f (±%.1fm)", loc.latitude, loc.longitude, loc.accuracy)
                } else {
                    "Location request timed out or unavailable"
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.category_connectivity)) },
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
                onStatusChange = { newStatus -> repository.setStatus(TestCategory.CONNECTIVITY, newStatus) },
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
            // Wi-Fi Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.conn_wifi), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = if (isWifiConnected) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    InfoRow(label = "Hardware Enabled", value = if (isWifiEnabled) "Yes" else "No")
                    InfoRow(label = "Network Status", value = if (isWifiConnected) "Connected (Active Route)" else "Disconnected")
                    InfoRow(label = stringResource(R.string.conn_wifi_ip), value = ipAddress)
                }
            }

            // Bluetooth Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.conn_bluetooth), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Bluetooth, contentDescription = null, tint = if (isBtEnabled) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    InfoRow(label = "Bluetooth Supported", value = if (isBtSupported) "Yes" else "No")
                    InfoRow(label = "Bluetooth Radio Enabled", value = if (isBtEnabled) "Yes" else "No")
                    InfoRow(label = "Bluetooth LE (BLE) Support", value = if (hasBle) "Yes" else "No")
                }
            }

            // NFC Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.conn_nfc), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Nfc, contentDescription = null, tint = if (isNfcEnabled) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    InfoRow(label = "NFC Chip Present", value = if (isNfcSupported) "Yes" else "No")
                    InfoRow(label = "NFC Enabled", value = if (isNfcEnabled) "Yes" else "No")
                }
            }

            // GPS / Location Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.conn_gps), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.GpsFixed, contentDescription = null, tint = if (isGpsEnabled) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    InfoRow(label = "GPS Hardware Provider", value = if (isGpsEnabled) "Available" else "Disabled in Settings")

                    if (locationResult != null) {
                        InfoRow(label = "Current Coordinates", value = locationResult ?: "")
                    }

                    Button(
                        onClick = {
                            if (!hasLocationPermission) {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            } else {
                                requestGpsLocation(context, locationManager) { loc ->
                                    locationResult = if (loc != null) {
                                        String.format(Locale.US, "Lat %.4f, Lon %.4f (±%.1fm)", loc.latitude, loc.longitude, loc.accuracy)
                                    } else {
                                        "No fix acquired yet"
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("request_gps_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.conn_gps_request))
                    }
                }
            }

            // Cellular Network Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.conn_cellular), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.CellTower, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    InfoRow(label = stringResource(R.string.conn_carrier), value = networkOperatorName)
                    InfoRow(label = "SIM Card Status", value = simStateStr)
                }
            }

            // USB State Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.conn_usb), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Usb, contentDescription = null, tint = if (isUsbConnected) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    InfoRow(label = "USB Cable State", value = if (isUsbConnected) "Connected" else "Disconnected")
                }
            }
        }
    }
}

private fun requestGpsLocation(context: Context, lm: LocationManager?, callback: (Location?) -> Unit) {
    if (lm == null) {
        callback(null)
        return
    }
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        callback(null)
        return
    }
    try {
        val lastGps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val lastNet = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        val best = if (lastGps != null && lastNet != null) {
            if (lastGps.time > lastNet.time) lastGps else lastNet
        } else lastGps ?: lastNet

        if (best != null) {
            callback(best)
            return
        }

        // Request single update
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                callback(location)
                try { lm.removeUpdates(this) } catch (_: Exception) {}
            }
            override fun onProviderDisabled(provider: String) {}
            override fun onProviderEnabled(provider: String) {}
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }
        lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null)
    } catch (_: Exception) {
        callback(null)
    }
}
