package io.github.kitswas.virtualgamepadmobile.ui.screens

import android.util.Log
import androidx.preference.PreferenceManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import io.github.kitswas.virtualgamepadmobile.ui.theme.ColorSchemePicker
import io.github.kitswas.virtualgamepadmobile.ui.theme.VirtualGamePadMobileTheme

@Composable
fun SettingsScreen(
    navController: NavController = rememberNavController()
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Settings Screen")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LocalContext.current)
        val prefEditor = remember {
            sharedPreferences.edit()
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp), contentPadding = PaddingValues(16.dp)
        ) {
            // Add dropdown for color scheme
            item {
                ColorSchemePicker { colorScheme ->
                    prefEditor.putInt("color_scheme", colorScheme.ordinal)
                }
            }

        }

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                sharedPreferences.edit().clear().apply()
                // Recompose the settings screen

            }) {
                Text("Reset")
            }

            Button(onClick = {
                val success = prefEditor.commit()
                Log.i("SettingsScreen", "Saved settings: $success")
                navController.popBackStack()
            }) {
                Text("Save")
            }

            Button(onClick = {
                navController.popBackStack()
            }) {
                Text("Back")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    VirtualGamePadMobileTheme {
        SettingsScreen()
    }
}
