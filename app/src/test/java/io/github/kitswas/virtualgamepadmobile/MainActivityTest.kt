package io.github.kitswas.virtualgamepadmobile

import org.junit.Assert.assertEquals
import org.junit.Test

class MainActivityTest {
    @Test
    fun layoutSelectionAlwaysOpensGamepadScreen() {
        assertEquals("gamepad", resolveLayoutDestination(isConnected = false))
        assertEquals("gamepad", resolveLayoutDestination(isConnected = true))
    }
}
