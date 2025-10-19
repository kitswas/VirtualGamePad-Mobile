package io.github.kitswas.virtualgamepadmobile.network

import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkCommandTest {
    @Test
    fun `SendGamepadState deep copy is not affected by original changes`() {
        val original = GamepadReading().apply {
            ButtonsDown = 1
            LeftTrigger = 0.5f
        }
        val command = NetworkCommand.SendGamepadState(original)
        // Change original after creating command
        original.ButtonsDown = 2
        original.LeftTrigger = 1.0f
        // The command's copy should not change
        assertEquals(1, command.gamepadState.ButtonsDown)
        assertEquals(0.5f, command.gamepadState.LeftTrigger, 0.001f)
    }
}
