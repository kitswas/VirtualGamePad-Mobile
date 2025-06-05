package io.github.kitswas.virtualgamepadmobile.network

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.LinkedBlockingQueue

class ConnectionViewModel : ViewModel() {

    private val tag = this::class.java.simpleName
    private var clientSocket: Socket? = null // Internal storage for the socket

    // Replace gamepad state queue with a general command queue
    private val commandQueue = LinkedBlockingQueue<NetworkCommand>()

    // Job for the command processor
    private var commandProcessorJob: Job? = null

    // Expose screen UI state
    private val _uiState = MutableStateFlow(ConnectionState())
    val uiState: StateFlow<ConnectionState> = _uiState.asStateFlow()

    /**
     * Enqueues a network command for processing.
     * This is the core method for all network operations.
     *
     * @param command The network command to enqueue
     */
    private fun enqueueCommand(command: NetworkCommand) {
        // Don't enqueue send commands if disconnected or disconnecting
        // But always allow Connect and Disconnect commands
        if (command !is NetworkCommand.Connect &&
            command !is NetworkCommand.Disconnect &&
            (_uiState.value.error != null || !_uiState.value.connected)
        ) {
            Log.d(tag, "Skipping command enqueue while disconnected: $command")
            return
        }

        commandQueue.offer(command)

        // Start the command processor if not already running
        if (commandProcessorJob == null || commandProcessorJob?.isActive != true) {
            startCommandProcessor()
        }
    }

    /**
     * Enqueues a gamepad state for sending.
     * This is the public API for sending gamepad updates.
     *
     * @param gamepadState The gamepad state to enqueue
     */
    fun enqueueGamepadState(gamepadState: GamepadReading) {
        enqueueCommand(NetworkCommand.SendGamepadState(gamepadState))
    }

    /**
     * Enqueues a string to send.
     * This is primarily for testing purposes.
     *
     * @param string The string to send
     */
    @Suppress("unused")
    fun enqueueString(string: String) {
        enqueueCommand(NetworkCommand.SendString(string))
    }

    /**
     * Connect to a server at the given IP address and port.
     * This now enqueues a connection command.
     */
    fun connect(ipAddress: String, port: Int) {
        _uiState.update {
            it.copy(
                isConnecting = true,
                ipAddress = ipAddress,
                port = port,
                error = null,
                connected = false
            )
        }

        enqueueCommand(NetworkCommand.Connect(ipAddress, port))
    }

    /**
     * Disconnect from the server.
     * This now enqueues a disconnect command.
     */
    fun disconnect() {
        enqueueCommand(NetworkCommand.Disconnect)
    }

    /**
     * Starts the command processor job that executes network commands.
     * Uses a blocking queue implementation to efficiently wait for new commands.
     */
    private fun startCommandProcessor() {
        // Cancel existing job if any
        commandProcessorJob?.cancel()

        // Start new processor job
        commandProcessorJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    // This will block until a command is available
                    val command = commandQueue.take()

                    // Process based on command type
                    when (command) {
                        is NetworkCommand.Connect -> processConnectCommand(command)
                        is NetworkCommand.SendGamepadState -> processSendGamepadStateCommand(command)
                        is NetworkCommand.SendString -> processSendStringCommand(command)
                        is NetworkCommand.Disconnect -> {
                            processDisconnectCommand()
                            // Exit the loop after disconnect
                            break
                        }
                    }
                } catch (e: InterruptedException) {
                    // The coroutine was canceled, just exit the loop
                    Log.d(tag, "Command processor interrupted: ${e.message}")
                    break
                } catch (e: CancellationException) {
                    // The coroutine was canceled, just exit the loop
                    Log.d(tag, "Command processor canceled")
                    break
                } catch (e: Exception) {
                    // Catch any other exceptions to prevent the loop from crashing
                    Log.e(tag, "Command processor error: ${e.message}", e)
                    _uiState.update {
                        it.copy(
                            error = "Unexpected error: ${e.message ?: "Unknown"}",
                            connected = false
                        )
                    }

                    // Force disconnect on fatal errors
                    try {
                        processDisconnectCommand()
                    } catch (e2: Exception) {
                        Log.e(tag, "Error during emergency disconnect: ${e2.message}")
                    }
                    break
                }
            }

            Log.d(tag, "Command processor stopped")
        }
    }

    /**
     * Process a connect command.
     */
    private fun processConnectCommand(command: NetworkCommand.Connect) {
        try {
            // Create and configure socket
            val socket = Socket()
            socket.tcpNoDelay = true
            socket.setPerformancePreferences(1, 2, 0)
            socket.setTrafficClass(0x10) // IPTOS_LOWDELAY
            // Use OS Timeout to get actual error, not just TimeoutException
            val timeout = 0 // in milliseconds, 0 means infinite

            try {
                socket.connect(java.net.InetSocketAddress(command.ipAddress, command.port), timeout)
                clientSocket = socket // Store the connected socket

                _uiState.update {
                    it.copy(
                        connected = true,
                        isConnecting = false,
                        error = null
                    )
                }
                Log.d(tag, "Connected: $clientSocket")
            } catch (e: IOException) {
                Log.e(tag, "Connection failed: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        connected = false,
                        isConnecting = false,
                        error = e.message ?: "Unknown connection error"
                    )
                }
                socket.close() // Ensure socket is closed on error
            }
        } catch (e: Exception) {
            Log.e(tag, "Connect error: ${e.message}", e)
            _uiState.update {
                it.copy(
                    connected = false,
                    isConnecting = false,
                    error = "Connect error: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    /**
     * Process a send gamepad state command.
     */
    private fun processSendGamepadStateCommand(command: NetworkCommand.SendGamepadState) {
        if (!_uiState.value.connected || clientSocket == null) {
            // Skip sending if we're not connected
            return
        }

        try {
            command.gamepadState.marshal(clientSocket!!.outputStream, null)
        } catch (e: IOException) {
            Log.e(tag, "Error sending gamepad state: ${e.message}", e)

            // Update UI with error and disconnect when socket errors occur
            _uiState.update {
                it.copy(
                    error = "Connection lost: ${e.message ?: "Unknown error"}",
                    connected = false  // Mark as disconnected immediately
                )
            }

            // Handle disconnection immediately instead of queuing
            if (e is SocketException) {
                Log.d(tag, "Socket exception detected, forcing disconnect")
                clientSocket?.close()
                clientSocket = null
                commandQueue.clear()

                // Throw exception to break out of the command processor loop
                throw e
            } else {
                // For other IO exceptions, try graceful disconnect
                commandQueue.clear()  // Clear other pending commands
                commandQueue.offer(NetworkCommand.Disconnect)  // Make sure we disconnect properly
            }
        }
    }

    /**
     * Process a send string command.
     */
    private fun processSendStringCommand(command: NetworkCommand.SendString) {
        if (!_uiState.value.connected || clientSocket == null) {
            // Skip sending if we're not connected
            return
        }

        try {
            clientSocket!!.outputStream.write(command.string.toByteArray())
        } catch (e: IOException) {
            Log.e(tag, "Error sending string: ${e.message}", e)

            // Update UI with error and disconnect when socket errors occur
            _uiState.update {
                it.copy(
                    error = "Connection lost: ${e.message ?: "Unknown error"}",
                    connected = false  // Mark as disconnected immediately
                )
            }

            // Handle disconnection immediately instead of queuing
            if (e is SocketException) {
                Log.d(tag, "Socket exception detected, forcing disconnect")
                clientSocket?.close()
                clientSocket = null
                commandQueue.clear()

                // Throw exception to break out of the command processor loop
                throw e
            } else {
                // For other IO exceptions, try graceful disconnect
                commandQueue.clear()  // Clear other pending commands
                commandQueue.offer(NetworkCommand.Disconnect)  // Make sure we disconnect properly
            }
        }
    }

    /**
     * Process a disconnect command.
     */
    private fun processDisconnectCommand() {
        try {
            clientSocket?.close()
        } catch (e: IOException) {
            Log.e(tag, "Error closing socket: ${e.message}", e)
        } finally {
            clientSocket = null

            // Clear the queue in case there are pending commands
            commandQueue.clear()

            _uiState.update {
                ConnectionState() // Reset to initial state
            }

            Log.d(tag, "Disconnected")
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Ensure disconnection when ViewModel is cleared
        if (_uiState.value.connected || clientSocket != null) {
            Log.d(tag, "ViewModel cleared, ensuring disconnection.")
            // Force immediate disconnect rather than queueing
            try {
                clientSocket?.close()
            } catch (e: IOException) {
                Log.e(tag, "Error closing socket during cleanup: ${e.message}", e)
            } finally {
                clientSocket = null
                commandQueue.clear()
                commandProcessorJob?.cancel()
                commandProcessorJob = null
            }
        }
    }
}
