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
import java.net.Socket

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

@Composable
fun ConnectMenu(
    navController: NavHostController = rememberNavController(),
    scanner: GmsBarcodeScanner?
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
                isIPValid =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        InetAddresses.isNumericAddress(ipAddress)
                    } else {
                        Patterns.IP_ADDRESS.matcher(ipAddress).matches()
                    }
                isPortValid = port.toIntOrNull() != null
            }
        }, shape = CircleShape) {
            Text(text = "Scan QR Code")
        }

        TextField(
            label = { Text(text = "IP Address") },
            value = ipAddress,
            onValueChange = {
                ipAddress = it
                isIPValid =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        InetAddresses.isNumericAddress(ipAddress)
                    } else {
                        Patterns.IP_ADDRESS.matcher(ipAddress).matches()
                    }
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
                isPortValid = port.toIntOrNull() != null
            },
            shape = RectangleShape,
            modifier = Modifier.padding(0.dp, 5.dp),
            isError = !isPortValid
        )

        Button(
            onClick =
            {
                CoroutineScope(Dispatchers.IO).launch {
                    connectAndSayHi(ipAddress, port.toInt())
                }
            },
            shape = CircleShape,
            enabled = isIPValid && isPortValid,
        ) {
            Text(text = "Connect")
        }
    }
}

private fun connectAndSayHi(ipAddress: String, port: Int) {
    try {
        val socket = Socket(ipAddress, port)
        Log.d("SocketHi", socket.toString())
        socket.outputStream.write("Hello from the client!\n".toByteArray())
        socket.close()
    } catch (e: Exception) {
        Log.e("SocketHi", e.toString())
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectMenuPreview() {
    VirtualGamePadMobileTheme {
        ConnectMenu(scanner = null)
    }
}
