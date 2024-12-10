package io.github.kitswas.virtualgamepadmobile.network

import android.util.Log
import androidx.lifecycle.ViewModel
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.net.Socket

class ConnectionViewModel : ViewModel() {

    private val tag = "ConnectionViewModel"

    // Expose screen UI state
    private val _uiState = MutableStateFlow(ConnectionState())
    val uiState: StateFlow<ConnectionState> = _uiState.asStateFlow()

    // Handle business logic
    /**
     * Connect to a server at the given IP address and port.
     * This is a blocking call.
     */
    fun connect(ipAddress: String, port: Int) {
        val socket = Socket()
        // Disable Nagle's algorithm
        socket.tcpNoDelay = true
        // low latency > short connection time > high bandwidth
        socket.setPerformancePreferences(1, 2, 0)
        // Mark traffic class for low latency
        // https://docs.oracle.com/javase/8/docs/api/java/net/Socket.html#setTrafficClass-int-
        socket.setTrafficClass(0x10) // IPTOS_LOWDELAY
        // This is a generous timeout to establish a connection
        // Typically the ping should be less than 50ms for gaming purposes
        val timeout = 0 // in milliseconds, 0 means infinite
        socket.connect(java.net.InetSocketAddress(ipAddress, port), timeout)
        Log.d(tag, socket.toString())

        _uiState.update { currentState ->
            currentState.copy(
                connected = true,
                ipAddress = ipAddress,
                port = port,
                socket = socket
            )
        }
    }

    /**
     * This is primarily for testing purposes.
     */
    @Suppress("unused")
    fun sendString(string: String) {
        if (_uiState.value.connected && _uiState.value.socket != null) {
            _uiState.value.socket!!.outputStream.write(string.toByteArray())
        }
    }

    fun sendGamepadState(gamepadState: GamepadReading) {
        if (_uiState.value.connected && _uiState.value.socket != null) {
            gamepadState.marshal(_uiState.value.socket!!.outputStream, null)
        }
    }

    fun disconnect() {
        _uiState.value.socket?.close()
        _uiState.update { currentState ->
            currentState.copy(
                connected = false,
                ipAddress = "",
                port = -1,
                socket = null
            )
        }
    }
}
