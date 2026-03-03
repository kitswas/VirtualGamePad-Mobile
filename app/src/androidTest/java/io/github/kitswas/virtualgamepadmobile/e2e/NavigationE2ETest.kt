package io.github.kitswas.virtualgamepadmobile.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.kitswas.virtualgamepadmobile.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testMainMenuNavigation() {
        // Main Menu to Connect Screen
        composeTestRule.onNodeWithText("Start").performClick()
        composeTestRule.onNodeWithText("Connect").assertIsDisplayed()
        composeTestRule.onNodeWithText("Scan QR Code").assertIsDisplayed()
        
        // Go back (simulating system back or UI back if available, here we don't have UI back on Connect screen, using system back)
        // Wait, ConnectMenu doesn't have a back button. I'll use Espresso to press back if needed, or just restart activity for simplicity in this test.
        // Actually, let's just test one path at a time or use pressBack()
    }

    @Test
    fun testNavigationToSettings() {
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed() // Title
        composeTestRule.onNodeWithText("Customize Gamepad Layout").performScrollTo().assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.onNodeWithText("Start").assertIsDisplayed()
    }

    @Test
    fun testNavigationToAbout() {
        composeTestRule.onNodeWithText("About").performClick()
        composeTestRule.onNodeWithText("🎮 Virtual GamePad Mobile").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Back").performClick()
        composeTestRule.onNodeWithText("Start").assertIsDisplayed()
    }

    @Test
    fun testNavigationToGamepadCustomization() {
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithText("Customize Gamepad Layout").performScrollTo().performClick()
        
        // Verify we are on customization screen
        composeTestRule.onNodeWithText("Gamepad Customization").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.onNodeWithText("Start").assertIsDisplayed()
    }
}
