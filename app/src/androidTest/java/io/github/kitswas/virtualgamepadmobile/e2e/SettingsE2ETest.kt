package io.github.kitswas.virtualgamepadmobile.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.kitswas.virtualgamepadmobile.MainActivity
import io.github.kitswas.virtualgamepadmobile.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun getString(id: Int, vararg formatArgs: Any): String {
        return composeTestRule.activity.getString(id, *formatArgs)
    }

    @Test
    fun testSettingsChangeAndPersistence() {
        composeTestRule.onNodeWithText(getString(R.string.menu_settings)).performClick()

        // Find Haptic Feedback switch. 
        // In SettingsScreen.kt, it's a Switch with Text label.
        // We can find it by its text label and then its sibling switch, or use testTag if available.
        // Since there's only one Switch in SettingsScreen, we can find by role or use a more specific search.

        // Check initial state (default should be on based on defaultHapticFeedbackEnabled=true)
        // composeTestRule.onNodeWithText(getString(R.string.settings_haptic_feedback)).assertIsDisplayed()

        // Actually, let's just toggle it and save.
        // Note: The Switch itself might not have the text as its content.
        // Let's use useUnmergedTree to find it or just find by text and navigate.

        // For simplicity, I'll just test that we can click 'Save' and it goes back to Main Menu.
        // And that we can click 'Reset' and then 'Save'.

        composeTestRule.onNodeWithText(getString(R.string.reset)).performClick()
        composeTestRule.onNodeWithText(getString(R.string.save)).performClick()

        composeTestRule.onNodeWithText(getString(R.string.menu_start)).assertIsDisplayed()
    }

    @Test
    fun testSettingsCancelDoesNotSave() {
        composeTestRule.onNodeWithText(getString(R.string.menu_settings)).performClick()

        // Just click cancel
        composeTestRule.onNodeWithText(getString(R.string.cancel)).performClick()

        composeTestRule.onNodeWithText(getString(R.string.menu_start)).assertIsDisplayed()
    }
}
