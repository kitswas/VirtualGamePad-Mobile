package io.github.kitswas.virtualgamepadmobile

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
import io.github.kitswas.virtualgamepadmobile.ui.theme.VirtualGamePadMobileTheme

@Composable
fun ConnectMenu(
    navController: NavHostController = rememberNavController()
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { }, shape = CircleShape) {
            Text(text = "Scan QR Code")
        }
        var ipAddress by remember { mutableStateOf("") }
        TextField(
            label = { Text(text = "IP Address") },
            value = ipAddress,
            onValueChange = { ipAddress = it },
            shape = RectangleShape,
            modifier = Modifier.padding(0.dp, 5.dp)
        )
        var port by remember { mutableStateOf("") }
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
        ConnectMenu()
    }
}
