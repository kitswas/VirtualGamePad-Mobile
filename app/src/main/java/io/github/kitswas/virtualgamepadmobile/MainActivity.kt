package io.github.kitswas.virtualgamepadmobile

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.kitswas.virtualgamepadmobile.ui.theme.VirtualGamePadMobileTheme

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VirtualGamePadMobileTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Holder()
                }
            }
        }
    }
}

@Composable
fun Holder() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main_menu") {
        composable("main_menu") {
            MainMenu(navController)
        }
        composable("connect_screen") {
            ConnectMenu(navController)
        }
    }
}

@Composable
@Preview(showBackground = true)
fun DefaultPreview() {
    VirtualGamePadMobileTheme {
        Holder()
    }
}
