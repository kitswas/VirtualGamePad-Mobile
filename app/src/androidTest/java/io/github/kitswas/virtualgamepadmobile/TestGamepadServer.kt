package io.github.kitswas.virtualgamepadmobile

import io.github.kitswas.VGP_Data_Exchange.GameButtons
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean

object TestGamepadServer {
    private val running = AtomicBoolean(false)
    private var serverSocket: ServerSocket? = null
    private val readings = Collections.synchronizedList(mutableListOf<GamepadReading>())

    fun start(port: Int) {
        if (running.getAndSet(true)) {
            println("[TestGamepadServer] Server is already running")
            return
        }

        try {
            serverSocket = ServerSocket(port)
            println("[TestGamepadServer] Listening on port ${serverSocket?.localPort} on interfaces:")
            // List all network interfaces and addresses
            Collections.list(NetworkInterface.getNetworkInterfaces())
                .filter { it.isUp && !it.isLoopback }.forEach { nif ->
                    Collections.list(nif.inetAddresses).forEach { address ->
                        println("[TestGamepadServer] ${nif.displayName}: $address")
                    }
                }

            // Start server loop in a separate thread
            Thread {
                try {
                    while (running.get()) {
                        try {
                            // Block waiting for client connection
                            val client = serverSocket?.accept()
                            if (client != null && running.get()) {
                                println("[TestGamepadServer] Client connected: ${client.remoteSocketAddress}")
                                client.tcpNoDelay = true
                                handleClient(client)
                                println("[TestGamepadServer] Client disconnected, ready for new connections")
                            }
                        } catch (e: Exception) {
                            if (running.get()) {
                                System.err.println("[TestGamepadServer] Error accepting client: $e")
                                Thread.sleep(100)
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (running.get()) {
                        System.err.println("[TestGamepadServer] Server error: $e")
                    }
                }
                println("[TestGamepadServer] Server loop ended")
            }.start()
        } catch (e: Exception) {
            System.err.println("[TestGamepadServer] Failed to start server: $e")
            running.set(false)
        }
    }

    fun stop() {
        if (!running.getAndSet(false)) {
            return
        }

        println("[TestGamepadServer] Stopping server...")
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            System.err.println("[TestGamepadServer] Error closing server socket: $e")
        }
        println("[TestGamepadServer] Server stopped")
    }

    fun getPort(): Int = serverSocket?.localPort ?: -1

    fun clearReadings() {
        readings.clear()
    }

    fun getReadings(): List<GamepadReading> {
        return readings.toList()
    }

    private fun handleClient(client: Socket) {
        try {
            client.use { socket ->
                socket.getInputStream().use { inputStream ->
                    val buf = ByteArray(1024)
                    while (running.get() && !socket.isClosed) {
                        try {
                            val len = inputStream.read(buf)
                            if (len == -1) {
                                break
                            }

                            var offset = 0
                            while (offset < len) {
                                try {
                                    val reading = GamepadReading()
                                    offset = reading.unmarshal(buf, offset, len)
                                    readings.add(reading)
                                    logGamepadReading(reading)
                                } catch (e: Exception) {
                                    System.err.println("[TestGamepadServer] Failed to unmarshal at offset $offset: $e")
                                    break
                                }
                            }
                        } catch (e: Exception) {
                            break
                        }
                    }
                }
            }
        } catch (e: Exception) {
            if (running.get()) {
                System.err.println("[TestGamepadServer] Client error: $e")
            }
        }
    }

    private fun getButtonNames(buttons: Int): String {
        return GameButtons.entries
            .filter { (buttons and it.value) != 0 }
            .joinToString(" | ") { it.name }
            .ifEmpty { "None" }
    }

    private fun logGamepadReading(r: GamepadReading) {
        val upNames = getButtonNames(r.ButtonsUp)
        val downNames = getButtonNames(r.ButtonsDown)
        println(
            "[GamepadReading] ButtonsUp: $upNames" +
                    ", ButtonsDown: $downNames" +
                    ", LeftThumbstickX: ${r.LeftThumbstickX}" +
                    ", LeftThumbstickY: ${r.LeftThumbstickY}" +
                    ", RightThumbstickX: ${r.RightThumbstickX}" +
                    ", RightThumbstickY: ${r.RightThumbstickY}" +
                    ", LeftTrigger: ${r.LeftTrigger}" +
                    ", RightTrigger: ${r.RightTrigger}"
        )
    }
}
