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

    @JvmStatic
    fun main(args: Array<String>) {
        val port = if (args.isNotEmpty()) args[0].toInt() else 7878
        start(port)

        // Add shutdown hook for clean server shutdown
        Runtime.getRuntime().addShutdownHook(Thread {
            println("[TestGamepadServer] Shutting down...")
            stop()
        })

        // Keep main thread alive
        try {
            while (running.get()) {
                Thread.sleep(1000)
            }
        } catch (_: InterruptedException) {
            stop()
        }
    }

    fun start(port: Int) {
        if (running.getAndSet(true)) {
            println("[TestGamepadServer] Server is already running")
            return
        }

        try {
            serverSocket = ServerSocket(port)
            println("[TestGamepadServer] Listening on port $port on interfaces:")
            // List all network interfaces and addresses
            Collections.list(NetworkInterface.getNetworkInterfaces())
                .filter { it.isUp && !it.isLoopback }.forEach { nif ->
                    Collections.list(nif.inetAddresses).forEach { address ->
                        println("[TestGamepadServer] ${nif.displayName}: $address")
                    }
                }
            println("[TestGamepadServer] Connect to 10.0.2.2:$port from your emulator")

            // Start server loop in a separate thread
            Thread {
                try {
                    while (running.get()) {
                        try {
                            // Block waiting for client connection (like production server)
                            val client = serverSocket?.accept()
                            if (client != null && running.get()) {
                                println("[TestGamepadServer] Client connected: ${client.remoteSocketAddress}")

                                // Configure socket like production server
                                client.tcpNoDelay = true

                                // Handle client in current thread (blocking, like production server)
                                // This mimics tcpServer->pauseAccepting() behavior
                                handleClient(client)

                                println("[TestGamepadServer] Client disconnected, ready for new connections")
                            }
                        } catch (e: Exception) {
                            if (running.get()) {
                                System.err.println("[TestGamepadServer] Error accepting client: $e")
                                // Small delay before retrying
                                Thread.sleep(1000)
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

        // Close server socket
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            System.err.println("[TestGamepadServer] Error closing server socket: $e")
        }

        println("[TestGamepadServer] Server stopped")
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
                                println("[TestGamepadServer] Client closed connection (EOF)")
                                break
                            }

                            // Process the received data
                            try {
                                val reading = GamepadReading()
                                reading.unmarshal(buf, 0, len)
                                logGamepadReading(reading)
                            } catch (e: Exception) {
                                // Handle partial reads or invalid data gracefully
                                System.err.println("[TestGamepadServer] Failed to unmarshal: $e")
                                System.err.println(
                                    "[TestGamepadServer] Raw bytes (len=$len): ${
                                        buf.copyOf(
                                            len
                                        ).joinToString(" ") { "%02x".format(it) }
                                    }"
                                )
                            }
                        } catch (e: Exception) {
                            if (running.get() && !socket.isClosed) {
                                System.err.println("[TestGamepadServer] Read error: $e")
                            }
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
