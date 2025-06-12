package io.github.kitswas.virtualgamepadmobile.ui.screens

import android.net.InetAddresses
import android.os.Build
import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kitswas.virtualgamepadmobile.data.PreviewBase
import io.github.kitswas.virtualgamepadmobile.data.PreviewHeightDp
import io.github.kitswas.virtualgamepadmobile.data.PreviewWidthDp
import io.github.kitswas.virtualgamepadmobile.network.ConnectionViewModel
import io.github.kitswas.virtualgamepadmobile.utils.QRScannerManager
import kotlinx.coroutines.CoroutineExceptionHandler
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
    val minPort = 1
    val maxPort = 65535
    return port.toIntOrNull().let { it != null && it in minPort..maxPort }
}

private fun attemptToConnect(
    connectionViewModel: ConnectionViewModel?,
    ipAddress: String,
    port: String,
    exceptionHandler: CoroutineExceptionHandler
) {
    if (connectionViewModel != null) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            connectionViewModel.connect(ipAddress, port.toInt())
            // The connection state will be updated in the ViewModel
        }
    }
}

@Composable
fun ConnectMenu(
    onNavigateToConnectingScreen: (String, String) -> Unit,
    onNavigateToModuleInstaller: () -> Unit,
    qrScannerManager: QRScannerManager,
    connectionViewModel: ConnectionViewModel?
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            var ipAddress by rememberSaveable { mutableStateOf("") }
            var port by rememberSaveable { mutableStateOf("") }
            var isIPValid by rememberSaveable { mutableStateOf(false) }
            var isPortValid by rememberSaveable { mutableStateOf(false) }
            val focusManager = LocalFocusManager.current
            Button(onClick = {
                // Check if modules are available
                val scanner = qrScannerManager.getQRScanner()
                if (scanner != null) {
                    // Modules are available, start scanning
                    val task = scanner.startScan()
                    task.addOnSuccessListener { barcode ->
                        val qrCode = barcode.rawValue ?: ""
                        Log.d("Scanned QR Code", qrCode)
                        ipAddress = getIP(qrCode)
                        port = getPort(qrCode)
                        // recalculate validity
                        isIPValid = validateIP(ipAddress)
                        isPortValid = validatePort(port)
                    }.addOnFailureListener { exception ->
                        Log.e("ConnectMenu", "QR scan failed: ${exception.message}")
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "QR scan failed: ${exception.message}",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                } else {
                    // Modules not available, navigate to installer
                    onNavigateToModuleInstaller()
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
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        // Pressing Ime button would move the text indicator's focus to the bottom
                        // field, if it exists!
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                ),
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
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        attemptToConnect(
                            connectionViewModel,
                            ipAddress,
                            port,
                            CoroutineExceptionHandler { _, e ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        duration = SnackbarDuration.Short,
                                        message = e.message
                                            ?: "Failed to connect to server: ${e.cause}",
                                    )
                                }
                            }
                        )
                    }
                ),
                shape = RectangleShape,
                modifier = Modifier.padding(0.dp, 5.dp),
                isError = !isPortValid
            )

            Button(
                onClick = {
                    if (isIPValid && isPortValid) {
                        // Navigate to the connecting screen with the IP and port
                        onNavigateToConnectingScreen(ipAddress, port)
                    } else {
                        scope.launch {
                            val errorMessage = when {
                                !isIPValid -> "Invalid IP address format"
                                !isPortValid -> "Invalid port number (must be between 1-65535)"
                                else -> "Invalid connection parameters"
                            }
                            snackbarHostState.showSnackbar(
                                message = errorMessage,
                                duration = SnackbarDuration.Short
                            )
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
}

@Preview(
    widthDp = PreviewWidthDp,
    heightDp = PreviewHeightDp,
)
@Composable
fun ConnectMenuPreview() {
    PreviewBase {
        ConnectMenu(
            onNavigateToConnectingScreen = { _, _ -> },
            onNavigateToModuleInstaller = { },
            qrScannerManager = QRScannerManager(LocalContext.current),
            connectionViewModel = null
        )
    }
}
