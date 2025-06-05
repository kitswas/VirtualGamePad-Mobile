package io.github.kitswas.virtualgamepadmobile.network

import io.github.kitswas.VGP_Data_Exchange.GamepadReading

/**
 * Represents a command to be executed on the network connection.
 * Commands are processed in a queue to maintain order and thread safety.
 */
sealed class NetworkCommand {
    /**
     * Command to connect to a server
     *
     * @param ipAddress The IP address of the server
     * @param port The port number to connect to
     */
    data class Connect(val ipAddress: String, val port: Int) : NetworkCommand()

    /**
     * Command to send a gamepad state to the server
     *
     * @param gamepadState The gamepad state to send (will be deep copied)
     */
    class SendGamepadState(originalState: GamepadReading) : NetworkCommand() {
        // Create a deep copy of the gamepad state to avoid mutation issues
        val gamepadState: GamepadReading = GamepadReading().apply {
            ButtonsDown = originalState.ButtonsDown
            ButtonsUp = originalState.ButtonsUp
            LeftThumbstickX = originalState.LeftThumbstickX
            LeftThumbstickY = originalState.LeftThumbstickY
            RightThumbstickX = originalState.RightThumbstickX
            RightThumbstickY = originalState.RightThumbstickY
            LeftTrigger = originalState.LeftTrigger
            RightTrigger = originalState.RightTrigger
        }
    }

    /**
     * Command to send a raw string to the server (for testing purposes)
     *
     * @param string The string data to send
     */
    data class SendString(val string: String) : NetworkCommand()

    /**
     * Command to disconnect from the server
     */
    object Disconnect : NetworkCommand()
}
