package io.github.kitswas.virtualgamepadmobile

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
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

fun getIP(qrCode: String): String {
    val splitTill = qrCode.lastIndexOf(":")
    if (splitTill == -1) return qrCode
    return qrCode.substring(0, splitTill)
}

fun getPort(qrCode: String): String {
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

        Button(onClick = {
            scanner?.startScan()?.addOnSuccessListener {
                val qrCode = it.rawValue ?: ""
                Log.d("Scanned QR Code", qrCode)
                ipAddress = getIP(qrCode)
                port = getPort(qrCode)
            }
        }, shape = CircleShape) {
            Text(text = "Scan QR Code")
        }

        TextField(
            label = { Text(text = "IP Address") },
            value = ipAddress,
            onValueChange = { ipAddress = it },
            shape = RectangleShape,
            modifier = Modifier.padding(0.dp, 5.dp)
        )

        TextField(
            label = { Text(text = "Port") },
            value = port,
            onValueChange = { port = it },
            shape = RectangleShape,
            modifier = Modifier.padding(0.dp, 5.dp)
        )

        Button(onClick = { }, shape = CircleShape) {
            Text(text = "Connect")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectMenuPreview() {
    VirtualGamePadMobileTheme {
        ConnectMenu(scanner = null)
    }
}
