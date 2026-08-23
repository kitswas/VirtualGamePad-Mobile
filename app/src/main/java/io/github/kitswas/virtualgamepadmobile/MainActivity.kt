package io.github.kitswas.virtualgamepadmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.kitswas.virtualgamepadmobile.data.SettingsRepository
import io.github.kitswas.virtualgamepadmobile.data.defaultBaseColor
import io.github.kitswas.virtualgamepadmobile.data.defaultColorScheme
import io.github.kitswas.virtualgamepadmobile.data.defaultFullScreenEnabled
import io.github.kitswas.virtualgamepadmobile.data.defaultHapticFeedbackEnabled
import io.github.kitswas.virtualgamepadmobile.data.defaultSaveConnectionCredentials
import io.github.kitswas.virtualgamepadmobile.network.ConnectionViewModel
import io.github.kitswas.virtualgamepadmobile.network.ConnectionViewModelFactory
import io.github.kitswas.virtualgamepadmobile.ui.navigation.Screen
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
        val hapticEnabled = settingsRepository.hapticFeedbackEnabled.collectAsStateWithLifecycle(
            initialValue = defaultHapticFeedbackEnabled
        )

        LaunchedEffect(hapticEnabled.value) {
            HapticUtils.isEnabled = hapticEnabled.value
        }

        val fullScreenEnabled = settingsRepository.fullScreenEnabled.collectAsStateWithLifecycle(
            initialValue = defaultFullScreenEnabled
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
            darkMode = settingsRepository.colorScheme.collectAsStateWithLifecycle(
                initialValue = defaultColorScheme
            ).value,
            baseColor = settingsRepository.baseColor.collectAsStateWithLifecycle(
                initialValue = defaultBaseColor
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
        NavHost(navController = navController, startDestination = Screen.MainMenu) {
            composable<Screen.MainMenu> {
                MainMenu(
                    onNavigateToConnectScreen = { navController.navigate(Screen.Connect) },
                    onNavigateToSettingsScreen = { navController.navigate(Screen.Settings) },
                    onNavigateToAboutScreen = { navController.navigate(Screen.About) },
                    onExit = { exitProcess(0) }
                )
            }
            composable<Screen.Connect> {
                val lastIpAddress by settingsRepository.lastConnectionIpAddress.collectAsStateWithLifecycle(
                    initialValue = ""
                )
                val lastPort by settingsRepository.lastConnectionPort.collectAsStateWithLifecycle(
                    initialValue = ""
                )
                val saveCredentials by settingsRepository.saveConnectionCredentials.collectAsStateWithLifecycle(
                    initialValue = defaultSaveConnectionCredentials
                )

                val initialIp = if (saveCredentials) lastIpAddress else ""
                val initialPort = if (saveCredentials) lastPort else ""

                ConnectMenu(
                    onNavigateToConnectingScreen = { ipAddress, port ->
                        navController.navigate(
                            Screen.Connecting(
                                ipAddress = ipAddress,
                                port = port
                            )
                        )
                    },
                    initialIp = initialIp,
                    initialPort = initialPort
                )
            }
            composable<Screen.Connecting> { backStackEntry ->
                val route: Screen.Connecting = backStackEntry.toRoute()
                ConnectingScreen(
                    onNavigateToGamepad = {
                        navController.navigate(Screen.Gamepad) {
                            // Pop connection screens so back button from Gamepad doesn't re-trigger connect
                            popUpTo<Screen.Connect> { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                    connectionViewModel = connectionViewModel,
                    ipAddress = route.ipAddress,
                    port = route.port
                )
            }
            composable<Screen.Gamepad> {
                GamePad(
                    connectionViewModel = connectionViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Screen.Settings> {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToGamepadCustomization = { navController.navigate(Screen.GamepadCustomization) },
                    settingsRepository = settingsRepository
                )
            }
            composable<Screen.GamepadCustomization> {
                GamepadCustomizationScreen(
                    onNavigateBack = { navController.popBackStack() },
                    settingsRepository = settingsRepository
                )
            }
            composable<Screen.About> {
                AboutScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
