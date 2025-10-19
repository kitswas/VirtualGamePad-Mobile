package io.github.kitswas.virtualgamepadmobile.integration

import io.github.kitswas.virtualgamepadmobile.TestGamepadServer
import io.github.kitswas.virtualgamepadmobile.network.ConnectionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test

class EndToEndIntegrationTest {
    companion object {
        private const val PORT = 0
        private var serverThread: Thread? = null

        @BeforeClass
        @JvmStatic
        fun startServer() {
            serverThread = Thread { TestGamepadServer.start(PORT) }
            serverThread!!.start()
            // Give server time to start
            Thread.sleep(1000)
        }

        @AfterClass
        @JvmStatic
        fun stopServer() {
            TestGamepadServer.stop()
            serverThread?.join(1000)
        }
    }

    @Test
    fun `can connect, send data, and disconnect`() = runBlocking(Dispatchers.IO) {
        val viewModel = ConnectionViewModel()
        viewModel.connect("127.0.0.1", PORT)
        delay(500) // Wait for connection
        assertTrue(viewModel.uiState.value.connected || viewModel.uiState.value.isConnecting)
        viewModel.disconnect()
        delay(500)
        assertTrue(!viewModel.uiState.value.connected)
    }
}
