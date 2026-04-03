package io.github.kitswas.virtualgamepadmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.kitswas.virtualgamepadmobile.data.SettingsRepository
import io.github.kitswas.virtualgamepadmobile.data.defaultBaseColor
import io.github.kitswas.virtualgamepadmobile.data.defaultColorScheme
import io.github.kitswas.virtualgamepadmobile.data.defaultFullScreenEnabled
import io.github.kitswas.virtualgamepadmobile.data.defaultHapticFeedbackEnabled
import io.github.kitswas.virtualgamepadmobile.data.defaultSaveConnectionCredentials
import io.github.kitswas.virtualgamepadmobile.network.ConnectionViewModel
import io.github.kitswas.virtualgamepadmobile.network.ConnectionViewModelFactory
import io.github.kitswas.virtualgamepadmobile.ui.screens.AboutScreen
import io.github.kitswas.virtualgamepadmobile.ui.screens.ConnectMenu
import io.github.kitswas.virtualgamepadmobile.ui.screens.ConnectingScreen
import io.github.kitswas.virtualgamepadmobile.ui.screens.GamePad
import io.github.kitswas.virtualgamepadmobile.ui.screens.GamepadCustomizationScreen
import io.github.kitswas.virtualgamepadmobile.ui.screens.MainMenu
import io.github.kitswas.virtualgamepadmobile.ui.screens.SettingsScreen
import io.github.kitswas.virtualgamepadmobile.ui.theme.VirtualGamePadMobileTheme
import io.github.kitswas.virtualgamepadmobile.ui.utils.HapticUtils
import kotlinx.coroutines.flow.first
import kotlin.system.exitProcess


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsRepository = SettingsRepository(this)

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Re-created activities receive the same ConnectionViewModel instance created by the first activity.
        // Use the 'by viewModels()' Kotlin property delegate with factory
        // from the activity-ktx artifact
        val connectionViewModel: ConnectionViewModel by viewModels {
            ConnectionViewModelFactory { ip, port ->
                if (settingsRepository.saveConnectionCredentials.first()) {
                    settingsRepository.setLastConnectionCredentials(ip, port.toString())
                }
            }
        }
        setContent {
            AppUI(
                connectionViewModel = connectionViewModel,
                settingsRepository = settingsRepository,
            )
        }
    }

    @Composable
    private fun AppUI(
        connectionViewModel: ConnectionViewModel,
        settingsRepository: SettingsRepository,
    ) {
        val hapticEnabled = settingsRepository.hapticFeedbackEnabled.collectAsState(
            initial = defaultHapticFeedbackEnabled
        )

        LaunchedEffect(hapticEnabled.value) {
            HapticUtils.isEnabled = hapticEnabled.value
        }

        val fullScreenEnabled = settingsRepository.fullScreenEnabled.collectAsState(
            initial = defaultFullScreenEnabled
        )

        LaunchedEffect(fullScreenEnabled.value) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (fullScreenEnabled.value) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }

        VirtualGamePadMobileTheme(
            darkMode = settingsRepository.colorScheme.collectAsState(
                initial = defaultColorScheme
            ).value,
            baseColor = settingsRepository.baseColor.collectAsState(
                initial = defaultBaseColor
            ).value
        ) {
            NavTree(
                connectionViewModel = connectionViewModel,
                settingsRepository = settingsRepository,
            )
        }
    }

    @Composable
    private fun NavTree(
        connectionViewModel: ConnectionViewModel,
        settingsRepository: SettingsRepository,
        navController: NavHostController = rememberNavController(),
    ) {
        NavHost(navController = navController, startDestination = "main_menu") {
            composable("main_menu") {
                MainMenu(
                    onNavigateToConnectScreen = { navController.navigate("connect_screen") },
                    onNavigateToSettingsScreen = { navController.navigate("settings_screen") },
                    onNavigateToAboutScreen = { navController.navigate("about_screen") },
                    onExit = { exitProcess(0) }
                )
            }
            composable("connect_screen") {
                val lastIpAddress by settingsRepository.lastConnectionIpAddress.collectAsState(
                    initial = ""
                )
                val lastPort by settingsRepository.lastConnectionPort.collectAsState(initial = "")
                val saveCredentials by settingsRepository.saveConnectionCredentials.collectAsState(
                    initial = defaultSaveConnectionCredentials
                )

                val initialIp = if (saveCredentials) lastIpAddress else ""
                val initialPort = if (saveCredentials) lastPort else ""

                ConnectMenu(
                    onNavigateToConnectingScreen = { ipAddress, port ->
                        navController.navigate("connecting_screen/$ipAddress/$port")
                    },
                    initialIp = initialIp,
                    initialPort = initialPort
                )
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
                    onNavigateToGamepad = {
                        navController.navigate("gamepad") {
                            popUpTo("connect_screen") { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                    connectionViewModel = connectionViewModel,
                    ipAddress = ipAddress,
                    port = port
                )
            }
            composable("gamepad") {
                GamePad(
                    connectionViewModel = connectionViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("settings_screen") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToGamepadCustomization = { navController.navigate("gamepad_customization") },
                    settingsRepository = settingsRepository
                )
            }
            composable("gamepad_customization") {
                GamepadCustomizationScreen(
                    onNavigateBack = { navController.popBackStack() },
                    settingsRepository = settingsRepository
                )
            }
            composable("about_screen") {
                AboutScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
