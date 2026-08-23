package io.github.kitswas.virtualgamepadmobile.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    data object MainMenu : Screen

    @Serializable
    data object Connect : Screen

    @Serializable
    data class Connecting(val ipAddress: String, val port: String) : Screen

    @Serializable
    data object Gamepad : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data object GamepadCustomization : Screen

    @Serializable
    data object About : Screen
}
