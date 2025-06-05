package io.github.kitswas.virtualgamepadmobile

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.common.moduleinstall.InstallStatusListener
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate.InstallState
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import io.github.kitswas.virtualgamepadmobile.data.SettingsRepository
import io.github.kitswas.virtualgamepadmobile.data.defaultBaseColor
import io.github.kitswas.virtualgamepadmobile.data.defaultColorScheme
import io.github.kitswas.virtualgamepadmobile.network.ConnectionViewModel
import io.github.kitswas.virtualgamepadmobile.ui.screens.ConnectMenu
import io.github.kitswas.virtualgamepadmobile.ui.screens.ConnectingScreen
import io.github.kitswas.virtualgamepadmobile.ui.screens.GamePad
import io.github.kitswas.virtualgamepadmobile.ui.screens.MainMenu
import io.github.kitswas.virtualgamepadmobile.ui.screens.SettingsScreen
import io.github.kitswas.virtualgamepadmobile.ui.theme.VirtualGamePadMobileTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
                            ).value,
                            settingsRepository.baseColor.collectAsState(
                                initial = defaultBaseColor
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
    private fun prepareQRScanner() {
        val options = GmsBarcodeScannerOptions.Builder().setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
        ).build()
        scanner = GmsBarcodeScanning.getClient(this, options)
        val moduleInstallClient = ModuleInstall.getClient(this)
        var areModulesAvailable = false
        moduleInstallClient.areModulesAvailable(scanner).addOnSuccessListener {
            areModulesAvailable = it.areModulesAvailable()
        }.addOnFailureListener {
            Log.w("ModuleInstaller", "Module detection failed: ${it.message}")
        }.addOnCompleteListener {
            if (!areModulesAvailable) {
                Log.i("ModuleInstaller", "Modules not found on device")

                class ModuleInstallProgressListener : InstallStatusListener {
                    override fun onInstallStatusUpdated(update: ModuleInstallStatusUpdate) {
                        update.progressInfo?.let {
                            val progress =
                                (it.bytesDownloaded * 100 / it.totalBytesToDownload).toInt()
                            Log.d(
                                "ModuleInstaller", "Module download progress: ${progress}%"
                            )
                        }
                        if (isTerminateState(update.installState)) {
                            val message = "Module installation terminated."
                            val state = when (update.installState) {
                                InstallState.STATE_CANCELED -> "Canceled"
                                InstallState.STATE_COMPLETED -> "Completed"
                                InstallState.STATE_FAILED -> "Failed"
                                else -> "Invalid"
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(
                                    this@MainActivity,
                                    "$message Please restart the app.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            Log.i("ModuleInstaller", "$message State: $state")
                        }
                    }

                    fun isTerminateState(@InstallState state: Int): Boolean {
                        return state == InstallState.STATE_CANCELED || state == InstallState.STATE_COMPLETED || state == InstallState.STATE_FAILED
                    }
                }

                val listener = ModuleInstallProgressListener()
                val moduleInstallRequest = ModuleInstallRequest.newBuilder().addApi(scanner)
                    // Add more APIs if you would like to request multiple optional modules.
                    // .addApi(...)
                    // Set the listener if you need to monitor the download progress.
                    .setListener(listener).build()
                Toast.makeText(
                    this,
                    "Installing modules. Please stay connected to the internet.",
                    Toast.LENGTH_LONG
                ).show()

                moduleInstallClient.installModules(moduleInstallRequest).addOnSuccessListener {
                    Log.i("ModuleInstaller", "Module installation requested")
                }.addOnFailureListener {
                    it.printStackTrace()
                    Log.w("ModuleInstaller", "Module installation failed: ${it.message}")
                    Toast.makeText(
                        this,
                        "Module installation failed: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Log.d("ModuleInstaller", "Modules found on device")
            }
        }
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
            composable(
                "connecting_screen/{ipAddress}/{port}",
                arguments = listOf(
                    navArgument("ipAddress") { type = NavType.StringType },
                    navArgument("port") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val ipAddress = backStackEntry.arguments?.getString("ipAddress") ?: ""
                val port = backStackEntry.arguments?.getString("port") ?: ""
                ConnectingScreen(
                    navController = navController,
                    connectionViewModel = connectionViewModel,
                    ipAddress = ipAddress,
                    port = port
                )
            }
            composable("gamepad") {
                GamePad(connectionViewModel, navController)
            }
            composable("settings_screen") {
                SettingsScreen(navController, settingsRepository)
            }
        }
    }
}
