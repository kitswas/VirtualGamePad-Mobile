package io.github.kitswas.virtualgamepadmobile.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.kitswas.virtualgamepadmobile.MainActivity
import io.github.kitswas.virtualgamepadmobile.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun getString(id: Int, vararg formatArgs: Any): String {
        return composeTestRule.activity.getString(id, *formatArgs)
    }

    @Test
    fun testMainMenuNavigation() {
        // Main Menu to Connect Screen
        composeTestRule.onNodeWithText(getString(R.string.menu_start)).performClick()
        composeTestRule.onNodeWithText(getString(R.string.connect_button)).assertIsDisplayed()
        composeTestRule.onNodeWithText(getString(R.string.connect_scan_qr)).assertIsDisplayed()
    }

    @Test
    fun testNavigationToSettings() {
        composeTestRule.onNodeWithText(getString(R.string.menu_settings)).performClick()
        composeTestRule.onNodeWithText(getString(R.string.settings_title))
            .assertIsDisplayed() // Title
        composeTestRule.onNodeWithText(getString(R.string.settings_customize_layout))
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText(getString(R.string.cancel)).performClick()
        composeTestRule.onNodeWithText(getString(R.string.menu_start)).assertIsDisplayed()
    }

    @Test
    fun testNavigationToAbout() {
        composeTestRule.onNodeWithText(getString(R.string.menu_about)).performClick()
        composeTestRule.onNodeWithText(getString(R.string.about_title)).assertIsDisplayed()

        composeTestRule.onNodeWithText(getString(R.string.back)).performScrollTo().performClick()
        composeTestRule.onNodeWithText(getString(R.string.menu_start)).assertIsDisplayed()
    }

    @Test
    fun testNavigationToGamepadCustomization() {
        composeTestRule.onNodeWithText(getString(R.string.menu_settings)).performClick()
        composeTestRule.onNodeWithText(getString(R.string.settings_customize_layout))
            .performScrollTo().performClick()

        // Verify we are on customization screen
        composeTestRule.onNodeWithText(getString(R.string.customization_title)).assertIsDisplayed()

        composeTestRule.onNodeWithText(getString(R.string.cancel)).performClick()
        composeTestRule.onNodeWithText(getString(R.string.settings_title)).assertIsDisplayed()

        composeTestRule.onNodeWithText(getString(R.string.cancel)).performClick()
        composeTestRule.onNodeWithText(getString(R.string.menu_start)).assertIsDisplayed()
    }
}
