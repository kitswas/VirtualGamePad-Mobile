package io.github.kitswas.virtualgamepadmobile.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.kitswas.virtualgamepadmobile.network.ConnectionViewModel
import kotlinx.coroutines.launch

@Composable
fun ConnectingScreen(
    onNavigateToGamepad: () -> Unit,
    onNavigateBack: () -> Unit,
    connectionViewModel: ConnectionViewModel?,
    ipAddress: String,
    port: String
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Get the current connection state
    val connectionState by connectionViewModel?.uiState?.collectAsState()
        ?: remember { mutableStateOf(null) }

    // Initiate connection when entering screen
    LaunchedEffect(ipAddress, port) {
        try {
            Log.d("ConnectingScreen", "Initiating connection to $ipAddress:$port")
            connectionViewModel?.connect(ipAddress, port.toInt())
        } catch (e: Exception) {
            Log.e("ConnectingScreen", "Failed to initiate connection: ${e.message}")
            snackbarHostState.showSnackbar(
                message = "Invalid connection parameters: ${e.message}",
                duration = SnackbarDuration.Short
            )
            onNavigateBack()
        }
    }

    // Watch for connection state changes
    LaunchedEffect(
        connectionState?.connected,
        connectionState?.error,
        connectionState?.isConnecting
    ) {
        connectionState?.let { state ->
            if (state.connected) {
                // Connection successful, navigate to gamepad
                Log.d("ConnectingScreen", "Connection successful, navigating to gamepad")
                onNavigateToGamepad()
            } else if (!state.isConnecting && state.error != null) {
                // Connection failed, show error and stay on screen
                Log.d("ConnectingScreen", "Connection failed: ${state.error}")
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Connection failed: ${state.error}",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    // Handle back navigation to cancel connection
    BackHandler {
        // Cancel any pending connection
        Log.d("ConnectingScreen", "Back pressed, canceling connection")
        connectionViewModel?.disconnect()
        onNavigateBack()
    }

    // UI for connecting screen
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
            if (connectionState?.connected == true) {
                Text("Connected! Redirecting...")
            } else if (connectionState?.isConnecting == true) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Connecting to $ipAddress:$port...")
            } else {
                // Show error message if there is one
                connectionState?.error?.let { error ->
                    Text(
                        text = "Connection Error",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        // Retry connection
                        connectionViewModel?.connect(ipAddress, port.toInt())
                    }) {
                        Text("Retry")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onNavigateBack) {
                        Text("Back")
                    }
                } ?: run {
                    // No error but also not connecting - initial state
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Preparing connection...")
                }
            }
        }
    }
}
