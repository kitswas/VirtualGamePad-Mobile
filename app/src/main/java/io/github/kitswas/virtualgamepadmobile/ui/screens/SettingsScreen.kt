package io.github.kitswas.virtualgamepadmobile.ui.screens

import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
@Composable
fun SettingsScreen(
    navController: NavController = rememberNavController()
) {
}


@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    VirtualGamePadMobileTheme {
        SettingsScreen()
    }
}
