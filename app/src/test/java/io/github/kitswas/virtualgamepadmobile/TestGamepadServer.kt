package io.github.kitswas.virtualgamepadmobile

import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import java.net.ServerSocket
import java.net.Socket

object TestGamepadServer {
    @JvmStatic
    fun main(args: Array<String>) {
        val port = if (args.isNotEmpty()) args[0].toInt() else 5555
        println("[TestGamepadServer] Listening on port $port")
        ServerSocket(port).use { serverSocket ->
            val client = serverSocket.accept()
            println("[TestGamepadServer] Client connected: ${client.remoteSocketAddress}")
            Thread { handleClient(client) }.start()
        }
    }

    private fun handleClient(client: Socket) {
        try {
            client.use { socket ->
                socket.getInputStream().use { inputStream ->
                    val buf = ByteArray(1024)
                    while (true) {
                        val len = inputStream.read(buf)
                        if (len == -1) {
                            break
                        }
                        val reading = GamepadReading()
                        try {
                            reading.unmarshal(buf, 0, len)
                            logGamepadReading(reading)
                        } catch (e: Exception) {
                            System.err.println("[TestGamepadServer] Failed to unmarshal: $e")
                            System.err.println(
                                "[TestGamepadServer] Raw bytes: ${
                                    buf.copyOf(len).contentToString()
                                }"
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            System.err.println("[TestGamepadServer] Client error: $e")
        } finally {
            println("[TestGamepadServer] Client disconnected")
        }
    }

    private fun logGamepadReading(r: GamepadReading) {
        println(
            "[GamepadReading] ButtonsUp: ${r.ButtonsUp}" +
                    ", ButtonsDown: ${r.ButtonsDown}" +
                    ", LeftTrigger: ${r.LeftTrigger}" +
                    ", RightTrigger: ${r.RightTrigger}" +
                    ", LeftThumbstickX: ${r.LeftThumbstickX}" +
                    ", LeftThumbstickY: ${r.LeftThumbstickY}" +
                    ", RightThumbstickX: ${r.RightThumbstickX}" +
                    ", RightThumbstickY: ${r.RightThumbstickY}"
        )
    }
}
