package io.github.kitswas.virtualgamepadmobile

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.kitswas.virtualgamepadmobile.data.AppLanguage
import io.github.kitswas.virtualgamepadmobile.data.ButtonAnchor
import io.github.kitswas.virtualgamepadmobile.data.ButtonComponent
import io.github.kitswas.virtualgamepadmobile.data.CustomProfileStorage
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
import io.github.kitswas.virtualgamepadmobile.ui.utils.LocaleHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

internal fun resolveLayoutDestination(isConnected: Boolean): String = "gamepad"

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Apply the saved language synchronously so the very first frame is
        // already in the right locale (Arabic by default).
        val requested = try {
            runBlocking {
                SettingsRepository(newBase).appLanguage.first()
            }
        } catch (_: Exception) {
            AppLanguage.Default
        }
        super.attachBaseContext(LocaleHelper.wrap(newBase, requested))
    }

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
        val hapticEnabled = settingsRepository.hapticFeedbackEnabled.collectAsState(
            initial = defaultHapticFeedbackEnabled
        )

        LaunchedEffect(hapticEnabled.value) {
            HapticUtils.isEnabled = hapticEnabled.value
        }

        val fullScreenEnabled = settingsRepository.fullScreenEnabled.collectAsState(
            initial = defaultFullScreenEnabled
        )

        val appLanguage by settingsRepository.appLanguage.collectAsState(
            initial = AppLanguage.Default
        )

        // When the user picks a language in Settings, apply it. The gamepad
        // itself is unaffected: it is forced to LTR (see GamePad) and the
        // socket lives in the ViewModel, so recreation only rebuilds labels.
        LaunchedEffect(appLanguage) {
            val current = LocaleHelper.currentLanguage(this@MainActivity)
            if (current != appLanguage) {
                LocaleHelper.applyToActivity(this@MainActivity, appLanguage)
            }
        }

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
            darkMode = defaultColorScheme,
            baseColor = defaultBaseColor
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
        // Activity-level orientation lock, driven by the current destination.
        // This is the authority (not the per-screen composable locks): it cannot
        // be lost to recomposition, so the gamepad can never fall back to the
        // manifest's portrait and pile its widgets on each other.
        androidx.compose.runtime.DisposableEffect(navController) {
            val listener = androidx.navigation.NavController.OnDestinationChangedListener { _, destination, _ ->
                requestedOrientation = when (destination.route) {
                    "gamepad", "gamepad_customization/{profileId}", "gamepad_customization_new" ->
                        android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    else ->
                        android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
            navController.addOnDestinationChangedListener(listener)
            onDispose { navController.removeOnDestinationChangedListener(listener) }
        }
        val connectionState by connectionViewModel.uiState.collectAsState(initial = null)
        val isConnected = connectionState?.connected == true
        val scope = rememberCoroutineScope()
        var customProfileRefreshKey by remember { mutableIntStateOf(0) }

        NavHost(navController = navController, startDestination = "main_menu") {
            composable("main_menu") {
                MainMenu(
                    isConnected = isConnected,
                    onConnectClick = { navController.navigate("connect_screen") },
                                        onProfileSelect = { profile ->
                        scope.launch {
                            val baseConfigs = io.github.kitswas.virtualgamepadmobile.data.defaultButtonConfigs
                            val context = this@MainActivity

                            val newLayout = when (profile.id) {
                                "racing_1" -> {
                                    baseConfigs.toMutableMap().apply {
                                        put(ButtonComponent.LEFT_ANALOG_STICK, this[ButtonComponent.LEFT_ANALOG_STICK]!!.copy(visible = false))
                                        put(ButtonComponent.FACE_BUTTONS, this[ButtonComponent.FACE_BUTTONS]!!.copy(anchor = ButtonAnchor.CENTER_LEFT, offsetX = 0.05f))
                                        put(ButtonComponent.DPAD, this[ButtonComponent.DPAD]!!.copy(anchor = ButtonAnchor.CENTER, offsetX = -0.2f))
                                        put(ButtonComponent.RIGHT_ANALOG_STICK, this[ButtonComponent.RIGHT_ANALOG_STICK]!!.copy(anchor = ButtonAnchor.CENTER, offsetX = 0.2f))
                                        put(ButtonComponent.LEFT_TRIGGER, this[ButtonComponent.LEFT_TRIGGER]!!.copy(anchor = ButtonAnchor.CENTER_RIGHT, offsetX = -0.3f, offsetY = 0f, scale = 2.2f))
                                        put(ButtonComponent.RIGHT_TRIGGER, this[ButtonComponent.RIGHT_TRIGGER]!!.copy(anchor = ButtonAnchor.CENTER_RIGHT, offsetX = -0.05f, offsetY = 0f, scale = 2.2f))
                                        put(ButtonComponent.LEFT_SHOULDER, this[ButtonComponent.LEFT_SHOULDER]!!.copy(anchor = ButtonAnchor.TOP_LEFT, offsetX = 0.05f))
                                        put(ButtonComponent.RIGHT_SHOULDER, this[ButtonComponent.RIGHT_SHOULDER]!!.copy(anchor = ButtonAnchor.TOP_LEFT, offsetX = 0.25f))
                                    }
                                }
                                "racing_2" -> {
                                    baseConfigs.toMutableMap().apply {
                                        put(ButtonComponent.LEFT_ANALOG_STICK, this[ButtonComponent.LEFT_ANALOG_STICK]!!.copy(visible = false))
                                        put(ButtonComponent.DPAD, this[ButtonComponent.DPAD]!!.copy(anchor = ButtonAnchor.CENTER_LEFT, offsetX = 0.05f, scale = 1.1f))
                                        put(ButtonComponent.FACE_BUTTONS, this[ButtonComponent.FACE_BUTTONS]!!.copy(anchor = ButtonAnchor.CENTER_RIGHT, offsetX = -0.05f))
                                        put(ButtonComponent.RIGHT_ANALOG_STICK, this[ButtonComponent.RIGHT_ANALOG_STICK]!!.copy(anchor = ButtonAnchor.BOTTOM_CENTER, offsetX = 0.35f, offsetY = -0.1f))
                                        put(ButtonComponent.LEFT_TRIGGER, this[ButtonComponent.LEFT_TRIGGER]!!.copy(anchor = ButtonAnchor.TOP_LEFT, offsetX = 0.05f, scale = 1.4f))
                                        put(ButtonComponent.RIGHT_TRIGGER, this[ButtonComponent.RIGHT_TRIGGER]!!.copy(anchor = ButtonAnchor.TOP_RIGHT, offsetX = -0.05f, scale = 1.4f))
                                    }
                                }
                                "game_controller" -> baseConfigs
                                else -> {
                                    val profileStorage = CustomProfileStorage(context)
                                    profileStorage.loadProfileConfigs(profile.id) ?: baseConfigs
                                }
                            }

                            settingsRepository.setAllButtonConfigs(newLayout)
                            settingsRepository.setActiveProfileId(profile.id)
                            navController.navigate("gamepad")
                        }
                    },
                    onCreateCustomProfile = {
                        navController.navigate("gamepad_customization_new")
                    },
                    onEditCustomProfile = { profile ->
                        navController.navigate("gamepad_customization/${profile.id}")
                    },
                    onNavigateToSettings = { navController.navigate("settings_screen") },
                    onNavigateToAbout = { navController.navigate("about_screen") },
                    refreshKey = customProfileRefreshKey
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
                    onNavigateToGamepadCustomization = {
                        navController.navigate("gamepad_customization/")
                    },
                    settingsRepository = settingsRepository
                )
            }
            composable(
                "gamepad_customization/{profileId}",
                arguments = listOf(
                    navArgument("profileId") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val profileId = backStackEntry.arguments?.getString("profileId").orEmpty()
                GamepadCustomizationScreen(
                    onNavigateBack = { navController.popBackStack() },
                    settingsRepository = settingsRepository,
                    profileId = profileId.takeIf { it.isNotEmpty() },
                    isNewProfile = false,
                    onProfileSaved = { customProfileRefreshKey += 1 }
                )
            }
            composable("gamepad_customization_new") {
                GamepadCustomizationScreen(
                    onNavigateBack = { navController.popBackStack() },
                    settingsRepository = settingsRepository,
                    isNewProfile = true,
                    onProfileSaved = {
                        customProfileRefreshKey += 1
                    }
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
