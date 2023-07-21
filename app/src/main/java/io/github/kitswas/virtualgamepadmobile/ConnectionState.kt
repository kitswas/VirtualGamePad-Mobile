package io.github.kitswas.virtualgamepadmobile

import android.util.Log
import androidx.lifecycle.ViewModel
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.net.Socket

data class ConnectionState(
    var connected: Boolean = false,
    val ipAddress: String = "",
    val port: Int = -1,
    var socket: Socket? = null
)

class ConnectionViewModel : ViewModel() {

    private val tag = "ConnectionViewModel"

    // Expose screen UI state
    private val _uiState = MutableStateFlow(ConnectionState())
    val uiState: StateFlow<ConnectionState> = _uiState.asStateFlow()

    // Handle business logic
    fun connect(ipAddress: String, port: Int) {
        try {
            val socket = Socket()
            // This is a generous timeout to establish a connection
            // Typically the ping should be less than 50ms for gaming purposes
            val timeout = 500 // in milliseconds, 0 means infinite
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
        } catch (e: Exception) {
            Log.e(tag, e.toString())
        }
    }

    fun sendString(string: String) {
        try {
            if (_uiState.value.connected && _uiState.value.socket != null) {
                _uiState.value.socket!!.outputStream.write(string.toByteArray())
            }
        } catch (e: Exception) {
            Log.e(tag, e.toString())
        }
    }

    fun sendGamepadState(gamepadState: GamepadReading) {
        try {
            if (_uiState.value.connected && _uiState.value.socket != null) {
                gamepadState.marshal(_uiState.value.socket!!.outputStream, null)
            }
        } catch (e: Exception) {
            Log.e(tag, e.toString())
        }
    }

    fun disconnect() {
        try {
            _uiState.value.socket?.close()
            _uiState.update { currentState ->
                currentState.copy(
                    connected = false,
                    ipAddress = "",
                    port = -1,
                    socket = null
                )
            }
        } catch (e: Exception) {
            Log.e(tag, e.toString())
        }
    }
}
