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

@RunWith(AndroidJUnit4::class)
class ConnectionE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

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
        
        composeTestRule.onNodeWithText("Start").performClick()
        
        composeTestRule.onNodeWithText("IP Address").performTextInput("127.0.0.1")
        composeTestRule.onNodeWithText("Port").performTextInput(port)
        
        composeTestRule.onNodeWithText("Connect").performClick()
        
        // Should navigate to ConnectingScreen then to GamePad
        // We might need to wait for connection
        composeTestRule.waitUntil(10000) {
            try {
                // Check if any element of Gamepad screen is visible
                composeTestRule.onNodeWithText("LSHLDR").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
}
