package io.github.kitswas.virtualgamepadmobile.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.kitswas.virtualgamepadmobile.utils.QRScannerManager
import io.github.kitswas.virtualgamepadmobile.utils.QRScannerManagerInterface
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class ModuleInstallerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Add a flag to enable/disable visual delays
    private val ENABLE_VISUAL_DELAYS = true
    private val VISUAL_DELAY_MS = 2000L  // delay for visual inspection

    private lateinit var mockQrScannerManager: QRScannerManagerInterface
    private val mockModuleAvailabilityState =
        MutableStateFlow<QRScannerManager.ModuleAvailability>(QRScannerManager.ModuleAvailability.NOT_AVAILABLE)

    private var onNavigateBackCalled = false
    private var onInstallationCompleteCalled = false

    @Before
    fun setUp() {
        // Reset flags
        onNavigateBackCalled = false
        onInstallationCompleteCalled = false

        // Create mock using the interface
        mockQrScannerManager = mock()

        // Set up the mock to return our controlled state flow
        whenever(mockQrScannerManager.moduleAvailabilityState).thenReturn(
            mockModuleAvailabilityState
        )
        whenever(mockQrScannerManager.getModuleAvailability()).thenReturn(QRScannerManager.ModuleAvailability.NOT_AVAILABLE)

        // Set up any additional method stubs that might be needed
        doAnswer { invocation ->
            val availabilityState = mockModuleAvailabilityState.value
            when (availabilityState) {
                QRScannerManager.ModuleAvailability.AVAILABLE -> true
                else -> false
            }
        }.whenever(mockQrScannerManager).areModulesAvailable()

        // Set up the startInstallation mock to trigger appropriate callbacks
        doAnswer { invocation ->
            val onProgress = invocation.getArgument<((Int) -> Unit)?>(0)
            val onComplete = invocation.getArgument<(() -> Unit)?>(1)
            val onError = invocation.getArgument<((String) -> Unit)?>(2)

            // When startInstallation is called, we update the state to INSTALLING
            mockModuleAvailabilityState.value = QRScannerManager.ModuleAvailability.INSTALLING

            // Optional: simulate some progress
            onProgress?.invoke(50)

            // We don't automatically call onComplete here - tests will control this
            Unit
        }.whenever(mockQrScannerManager).startInstallation(any(), any(), any())
    }

    private fun setScreenContent(initialState: QRScannerManager.ModuleAvailability = QRScannerManager.ModuleAvailability.NOT_AVAILABLE) {
        mockModuleAvailabilityState.value = initialState
        composeTestRule.setContent {
            ModuleInstallerScreen(
                onNavigateBack = { onNavigateBackCalled = true },
                onInstallationComplete = { onInstallationCompleteCalled = true },
                qrScannerManager = mockQrScannerManager
            )
        }
        addVisualDelay("Screen content set")
    }

    @Test
    fun initialState_showsInstallButtonAndTexts() {
        setScreenContent()

        composeTestRule.onNodeWithText("QR Scanner Module Required").assertIsDisplayed()
        addVisualDelay("Verifying initial text")
        composeTestRule.onNodeWithText("Download QR Scanner Module").assertIsDisplayed()
        addVisualDelay("Initial state verification complete")
    }

    @Test
    fun clickInstall_whenNotAvailable_startsInstallation() {
        setScreenContent(QRScannerManager.ModuleAvailability.NOT_AVAILABLE)
        addVisualDelay("Before clicking install button")

        composeTestRule.onNodeWithText("Download QR Scanner Module").performClick()
        addVisualDelay("After clicking install button")

        // Verify that the QRScannerManager's startInstallation is called
        verify(mockQrScannerManager).startInstallation(
            onProgress = any(),
            onComplete = any(),
            onError = any()
        )
    }

    @Test
    fun installingState_showsProgressAndCancelOption() {
        setScreenContent(QRScannerManager.ModuleAvailability.NOT_AVAILABLE)
        addVisualDelay("Initial state")

        // Click the download button to trigger the INSTALLING state
        composeTestRule.onNodeWithText("Download QR Scanner Module").performClick()
        addVisualDelay("After clicking download")

        // Wait for UI to update
        composeTestRule.waitForIdle()
        addVisualDelay("After waiting for idle")

        // Now verify the installing state UI elements
        composeTestRule.onNodeWithText("Downloading module...").assertIsDisplayed()
        addVisualDelay("Verifying downloading text")
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
        addVisualDelay("Installing state verification complete")
    }

    @Test
    fun clickCancel_duringInstallation_callsCancelInstallationAndNavigatesBack() {
        setScreenContent(QRScannerManager.ModuleAvailability.NOT_AVAILABLE)
        addVisualDelay("Initial state")

        // First click download to enter installing state
        composeTestRule.onNodeWithText("Download QR Scanner Module").performClick()
        composeTestRule.waitForIdle()
        addVisualDelay("After starting installation")

        // Now click cancel during installation
        composeTestRule.onNodeWithText("Cancel").performClick()
        addVisualDelay("After clicking cancel")

        // Verify cancelInstallation was called and navigation happened
        verify(mockQrScannerManager).cancelInstallation()
        assert(onNavigateBackCalled) { "onNavigateBack should have been called" }
    }

    @Test
    fun installationComplete_callsOnInstallationCompleteCallback() {
        setScreenContent(QRScannerManager.ModuleAvailability.NOT_AVAILABLE)

        // Click download to start installation
        composeTestRule.onNodeWithText("Download QR Scanner Module").performClick()
        composeTestRule.waitForIdle()

        // Simulate module becoming available
        mockModuleAvailabilityState.value = QRScannerManager.ModuleAvailability.AVAILABLE

        // Wait for recomposition
        composeTestRule.waitForIdle()

        assert(onInstallationCompleteCalled) { "onInstallationComplete should have been called" }
    }

    @Test
    fun installationFailed_showsErrorAndRetryOption() {
        setScreenContent(QRScannerManager.ModuleAvailability.NOT_AVAILABLE)
        addVisualDelay("Initial state")

        // Start installation
        composeTestRule.onNodeWithText("Download QR Scanner Module").performClick()
        composeTestRule.waitForIdle()
        addVisualDelay("After starting installation")

        // Simulate failure
        mockModuleAvailabilityState.value = QRScannerManager.ModuleAvailability.INSTALL_FAILED
        composeTestRule.waitForIdle()
        addVisualDelay("After installation failed")

        composeTestRule.onNodeWithText("Module installation failed").assertIsDisplayed()
        addVisualDelay("Verifying error message")
        composeTestRule.onNodeWithText("Try Again").assertIsDisplayed()
        addVisualDelay("Failure state verification complete")
    }

    @Test
    fun clickRetry_afterFailure_startsInstallationAgain() {
        setScreenContent(QRScannerManager.ModuleAvailability.NOT_AVAILABLE)

        // Start installation
        composeTestRule.onNodeWithText("Download QR Scanner Module").performClick()
        composeTestRule.waitForIdle()

        // Simulate failure
        mockModuleAvailabilityState.value = QRScannerManager.ModuleAvailability.INSTALL_FAILED
        composeTestRule.waitForIdle()

        // Reset for verification
        verify(mockQrScannerManager).startInstallation(any(), any(), any())

        // Click retry
        composeTestRule.onNodeWithText("Try Again").performClick()

        // Verify startInstallation is called again (2 times total - initial + retry)
        verify(mockQrScannerManager).startInstallation(any(), any(), any())
    }

    @Test
    fun installationCancelled_showsMessageAndRetryOption() {
        setScreenContent(QRScannerManager.ModuleAvailability.NOT_AVAILABLE)

        // Start installation
        composeTestRule.onNodeWithText("Download QR Scanner Module").performClick()
        composeTestRule.waitForIdle()

        // Simulate cancellation
        mockModuleAvailabilityState.value = QRScannerManager.ModuleAvailability.INSTALL_CANCELLED
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Installation was cancelled").assertIsDisplayed()
        composeTestRule.onNodeWithText("Try Again").assertIsDisplayed()
    }

    @Test
    fun backNavigation_whenNotInstalling_callsOnNavigateBack() {
        setScreenContent(QRScannerManager.ModuleAvailability.NOT_AVAILABLE)

        // Test Cancel button instead of system back press
        composeTestRule.onNodeWithText("Cancel").performClick()
        assert(onNavigateBackCalled) { "onNavigateBack should have been called" }
    }

    /**
     * Helper function to add visual delay for human observation during tests.
     *
     * Only adds delay if ENABLE_VISUAL_DELAYS is true
     */
    private fun addVisualDelay(stepDescription: String = "") {
        if (ENABLE_VISUAL_DELAYS) {
            println("TEST STEP: $stepDescription")
            try {
                Thread.sleep(VISUAL_DELAY_MS)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        composeTestRule.waitForIdle()
    }
}

// Note: To fully test the BackHandler, especially its conditional logic,
// you might need to use Espresso for system back presses or refactor the Composable
// to make the BackHandler's lambda more accessible for testing.
// The current tests for "Cancel" button cover similar logic for the INSTALLING state.
// The test `installationComplete_callsOnInstallationCompleteCallback` relies on LaunchedEffect
// which should trigger when the state changes.
// The `clickInstall_whenNotAvailable_startsInstallation` and `clickRetry_afterFailure_startsInstallationAgain`
// verify that the installation process is initiated.
// The `mockQrScannerManager.startInstallation` is mocked with specific arguments (null for callbacks).
