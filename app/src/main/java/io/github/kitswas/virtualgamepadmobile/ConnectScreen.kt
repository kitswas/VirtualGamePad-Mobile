package io.github.kitswas.virtualgamepadmobile

import android.net.InetAddresses
import android.os.Build
import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import io.github.kitswas.virtualgamepadmobile.ui.theme.VirtualGamePadMobileTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private fun getIP(qrCode: String): String {
    val splitTill = qrCode.lastIndexOf(":")
    if (splitTill == -1) return qrCode
    return qrCode.substring(0, splitTill)
}

private fun getPort(qrCode: String): String {
    val splitAt = qrCode.lastIndexOf(":")
    if (splitAt == -1) return qrCode
    return qrCode.substring(splitAt + 1)
}

@Suppress("DEPRECATION")
fun validateIP(ipAddress: String): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        InetAddresses.isNumericAddress(ipAddress)
    } else {
        Patterns.IP_ADDRESS.matcher(ipAddress).matches()
    }
}

fun validatePort(port: String): Boolean {
    return port.toIntOrNull().let { it != null && it in 1..65535 }
}

@Composable
fun ConnectMenu(
    navController: NavHostController = rememberNavController(),
    scanner: GmsBarcodeScanner?,
    connectionViewModel: ConnectionViewModel?
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var ipAddress by remember { mutableStateOf("") }
        var port by remember { mutableStateOf("") }
        var isIPValid by remember { mutableStateOf(false) }
        var isPortValid by remember { mutableStateOf(false) }

        Button(onClick = {
            scanner?.startScan()?.addOnSuccessListener {
                val qrCode = it.rawValue ?: ""
                Log.d("Scanned QR Code", qrCode)
                ipAddress = getIP(qrCode)
                port = getPort(qrCode)
                // recalculate validity
                isIPValid = validateIP(ipAddress)
                isPortValid = validatePort(port)
            }
        }, shape = CircleShape) {
            Text(text = "Scan QR Code")
        }

        TextField(
            label = { Text(text = "IP Address") },
            value = ipAddress,
            onValueChange = {
                ipAddress = it
                isIPValid = validateIP(ipAddress)
            },
            shape = RectangleShape,
            modifier = Modifier.padding(0.dp, 5.dp),
            isError = !isIPValid
        )

        TextField(
            label = { Text(text = "Port") },
            value = port,
            onValueChange = {
                port = it
                isPortValid = validatePort(port)
            },
            shape = RectangleShape,
            modifier = Modifier.padding(0.dp, 5.dp),
            isError = !isPortValid
        )

        Button(
            onClick =
            {
                if (connectionViewModel != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        connectionViewModel.connect(ipAddress, port.toInt())
                    }.invokeOnCompletion {
                        CoroutineScope(Dispatchers.Main).launch {
                            // Update UI elements
                            if (connectionViewModel.uiState.value.connected) {
                                navController.navigate("gamepad")
                            }
                        }
                    }
                }
            },
            shape = CircleShape,
            enabled = isIPValid && isPortValid,
        ) {
            Text(text = "Connect")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectMenuPreview() {
    VirtualGamePadMobileTheme {
        ConnectMenu(scanner = null, connectionViewModel = null)
    }
}
