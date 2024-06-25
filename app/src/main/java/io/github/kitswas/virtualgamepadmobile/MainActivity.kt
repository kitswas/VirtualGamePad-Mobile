package io.github.kitswas.virtualgamepadmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import io.github.kitswas.virtualgamepadmobile.data.SettingsRepository
import io.github.kitswas.virtualgamepadmobile.data.defaultColorScheme
import io.github.kitswas.virtualgamepadmobile.network.ConnectionViewModel
import io.github.kitswas.virtualgamepadmobile.ui.screens.ConnectMenu
import io.github.kitswas.virtualgamepadmobile.ui.screens.GamePad
import io.github.kitswas.virtualgamepadmobile.ui.screens.MainMenu
import io.github.kitswas.virtualgamepadmobile.ui.screens.SettingsScreen
import io.github.kitswas.virtualgamepadmobile.ui.theme.VirtualGamePadMobileTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var scanner: GmsBarcodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepareQRScanner()
        val settingsRepository = SettingsRepository(this)

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Re-created activities receive the same ConnectionViewModel instance created by the first activity.

        // Use the 'by viewModels()' Kotlin property delegate
        // from the activity-ktx artifact
        val connectionViewModel: ConnectionViewModel by viewModels()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                connectionViewModel.uiState.collect {
                    // Update UI elements
                    setContent {

                        VirtualGamePadMobileTheme(
                            settingsRepository.colorScheme.collectAsState(
                                initial = defaultColorScheme
                            ).value
                        ) {
                            // A surface container using the 'background' color from the theme
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.background
                            ) {
                                NavTree(connectionViewModel, settingsRepository)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * https://developers.google.com/ml-kit/vision/barcode-scanning/code-scanner
     */
    fun prepareQRScanner() {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
            )
            .build()
        scanner = GmsBarcodeScanning.getClient(this, options)
    }

    @Composable
    fun NavTree(
        connectionViewModel: ConnectionViewModel,
        settingsRepository: SettingsRepository,
    ) {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "main_menu") {
            composable("main_menu") {
                MainMenu(navController)
            }
            composable("connect_screen") {
                ConnectMenu(navController, scanner, connectionViewModel)
            }
            composable("gamepad") {
                GamePad(connectionViewModel)
            }
            composable("settings_screen") {
                SettingsScreen(navController, settingsRepository)
            }
        }
    }
}
