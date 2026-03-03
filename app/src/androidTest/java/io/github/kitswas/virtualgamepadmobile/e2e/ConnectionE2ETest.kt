package io.github.kitswas.virtualgamepadmobile.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.kitswas.virtualgamepadmobile.MainActivity
import io.github.kitswas.virtualgamepadmobile.TestGamepadServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import io.github.kitswas.virtualgamepadmobile.R

@RunWith(AndroidJUnit4::class)
class ConnectionE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun getString(id: Int, vararg formatArgs: Any): String {
        return composeTestRule.activity.getString(id, *formatArgs)
    }

    @Before
    fun setup() {
        TestGamepadServer.start(0) // Start on random port
    }

    @After
    fun teardown() {
        TestGamepadServer.stop()
    }

    @Test
    fun testSuccessfulConnection() {
        val port = TestGamepadServer.getPort().toString()

        composeTestRule.onNodeWithText(getString(R.string.menu_start)).performClick()

        composeTestRule.onNodeWithText(getString(R.string.connect_ip_label)).performTextInput("127.0.0.1")
        composeTestRule.onNodeWithText(getString(R.string.connect_port_label)).performTextInput(port)

        composeTestRule.onNodeWithText(getString(R.string.connect_button)).performClick()

        // Should navigate to ConnectingScreen then to GamePad
        // We might need to wait for connection
        composeTestRule.waitUntil(10000) {
            try {
                // Check if any element of Gamepad screen is visible
                composeTestRule.onNodeWithText(getString(R.string.button_l_shoulder)).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
}
