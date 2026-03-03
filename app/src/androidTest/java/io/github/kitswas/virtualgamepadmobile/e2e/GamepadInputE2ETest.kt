package io.github.kitswas.virtualgamepadmobile.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.kitswas.VGP_Data_Exchange.GameButtons
import io.github.kitswas.virtualgamepadmobile.MainActivity
import io.github.kitswas.virtualgamepadmobile.TestGamepadServer
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GamepadInputE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        TestGamepadServer.start(0)
    }

    @After
    fun teardown() {
        TestGamepadServer.stop()
    }

    @Test
    fun testButtonInputs() {
        val port = TestGamepadServer.getPort().toString()
        println("Connecting to port: $port")
        
        // Connect to server
        composeTestRule.onNodeWithText("Start").performClick()
        composeTestRule.onNodeWithText("IP Address").performTextInput("127.0.0.1")
        composeTestRule.onNodeWithText("Port").performTextInput(port)
        composeTestRule.onNodeWithText("Connect").performClick()
        
        println("Waiting for Gamepad screen...")
        // Wait for Gamepad screen
        composeTestRule.waitUntil(10000) {
            try {
                composeTestRule.onNodeWithText("LSHLDR").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        println("Gamepad screen displayed.")
        
        // Clear any readings from the connection process
        TestGamepadServer.clearReadings()
        
        println("Pressing Face Button A")
        // Press Face Button A
        composeTestRule.onNodeWithText("A").performTouchInput {
            down(center)
            advanceEventTime(1000)
            up()
        }
        
        println("Waiting for A button press reading...")
        composeTestRule.waitUntil(10000) {
            TestGamepadServer.getReadings().any { 
                (it.ButtonsDown and GameButtons.A.value) != 0
            }
        }
        println("A button press received.")
        
        assertTrue("Server should have received A button press", TestGamepadServer.getReadings().any { (it.ButtonsDown and GameButtons.A.value) != 0 })
        
        // Press Dpad Up
        TestGamepadServer.clearReadings()
        println("Pressing Dpad UP")
        composeTestRule.onNodeWithContentDescription("Dpad Button UP").performTouchInput {
            down(center)
            advanceEventTime(1000)
            up()
        }
        
        println("Waiting for Dpad UP reading...")
        composeTestRule.waitUntil(10000) {
            TestGamepadServer.getReadings().any { (it.ButtonsDown and GameButtons.DPadUp.value) != 0 }
        }
        println("Dpad UP received.")
        
        // Press LT
        TestGamepadServer.clearReadings()
        println("Pressing LT")
        composeTestRule.onNodeWithText("LT").performTouchInput {
            down(center)
            advanceEventTime(1000)
            up()
        }
        
        println("Waiting for LT reading...")
        composeTestRule.waitUntil(10000) {
            TestGamepadServer.getReadings().any { it.LeftTrigger > 0.5f }
        }
        println("LT received.")
        
        // Move Left Analog Stick
        TestGamepadServer.clearReadings()
        println("Moving Analog Stick RIGHT")
        composeTestRule.onNodeWithTag("AnalogStick_LEFT_Handle").performTouchInput {
            swipeRight()
        }
        
        println("Waiting for Analog Stick reading...")
        composeTestRule.waitUntil(10000) {
            TestGamepadServer.getReadings().any { it.LeftThumbstickX > 0.1f }
        }
        println("Analog Stick received.")
        
        // Move Right Analog Stick
        TestGamepadServer.clearReadings()
        println("Moving Right Analog Stick RIGHT")
        composeTestRule.onNodeWithTag("AnalogStick_RIGHT_Handle").performTouchInput {
            swipeRight()
        }
        
        println("Waiting for Right Analog Stick reading...")
        composeTestRule.waitUntil(10000) {
            TestGamepadServer.getReadings().any { it.RightThumbstickX > 0.1f }
        }
        println("Right Analog Stick received.")
    }
}
