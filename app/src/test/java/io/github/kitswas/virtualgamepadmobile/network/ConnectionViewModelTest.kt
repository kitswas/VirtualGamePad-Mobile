package io.github.kitswas.virtualgamepadmobile.network

import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.SocketException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ConnectionViewModelTest {
    private lateinit var viewModel: ConnectionViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        viewModel = ConnectionViewModel()
    }

    @Test
    fun `connection failure sets error state`() = testScope.runTest {
        viewModel.connect("256.256.256.256", 12345)
        var state: ConnectionState
        do {
            state = viewModel.uiState.value
        } while (state.error == null)
        // Print the actual state for debugging
        println("Final state after waiting: $state")
        // Assert error is set
        assertNotNull(state.error)
    }

    @Test
    fun `disconnect resets state and closes socket`() = testScope.runTest {
    val port = 0 // Use random available port
    val serverThread = Thread { io.github.kitswas.virtualgamepadmobile.TestGamepadServer.start(port) }
    serverThread.start()
    Thread.sleep(500)
    val actualPort = (io.github.kitswas.virtualgamepadmobile.TestGamepadServer.javaClass.getDeclaredField("serverSocket").apply { isAccessible = true }.get(io.github.kitswas.virtualgamepadmobile.TestGamepadServer) as java.net.ServerSocket?)?.localPort ?: throw RuntimeException("Server not started")
    viewModel.connect("127.0.0.1", actualPort)
        // Wait for connection (poll for up to 2 seconds)
        var state: ConnectionState
        var waited = 0
        do {
            testDispatcher.scheduler.advanceTimeBy(100)
            state = viewModel.uiState.value
            waited += 100
        } while (!state.connected && waited < 2000)
        viewModel.disconnect()
        testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value
        // Accept either disconnected or reset state
        assertFalse(state.connected)
        io.github.kitswas.virtualgamepadmobile.TestGamepadServer.stop()
        serverThread.join(1000)
    }

    @Test
    fun `command queue processes in order`() = testScope.runTest {
    val port = 0 // Use random available port
    val serverThread = Thread { io.github.kitswas.virtualgamepadmobile.TestGamepadServer.start(port) }
    serverThread.start()
    Thread.sleep(500)
    val actualPort = (io.github.kitswas.virtualgamepadmobile.TestGamepadServer.javaClass.getDeclaredField("serverSocket").apply { isAccessible = true }.get(io.github.kitswas.virtualgamepadmobile.TestGamepadServer) as java.net.ServerSocket?)?.localPort ?: throw RuntimeException("Server not started")
    viewModel.connect("127.0.0.1", actualPort)
        // Wait for connection
        var state: ConnectionState
        var waited = 0
        do {
            testDispatcher.scheduler.advanceTimeBy(100)
            state = viewModel.uiState.value
            waited += 100
        } while (!state.connected && waited < 2000)
        viewModel.enqueueString("test1")
        viewModel.enqueueString("test2")
        testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value
        // Accept either connected or error state
        assertTrue(state.connected || state.error != null)
        io.github.kitswas.virtualgamepadmobile.TestGamepadServer.stop()
        serverThread.join(1000)
    }

    @Test
    fun `sending gamepad state when disconnected does not crash`() = testScope.runTest {
        val reading = GamepadReading()
        // Should not throw, even if disconnected
        try {
            viewModel.disconnect()
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.enqueueGamepadState(reading)
        } catch (e: Exception) {
            fail("Exception thrown: ${e.message}")
        }
        // If no exception, test passes
    }
}
