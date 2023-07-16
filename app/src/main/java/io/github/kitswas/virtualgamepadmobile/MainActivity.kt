package io.github.kitswas.virtualgamepadmobile

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import io.github.kitswas.virtualgamepadmobile.ui.theme.VirtualGamePadMobileTheme
import kotlin.system.exitProcess

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VirtualGamePadMobileTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    MainMenu()
                }
            }
        }
    }
}

@Composable
fun MainMenu() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var name by remember { mutableStateOf("Start") }
        Button(onClick = { name = "Clicked..." }, shape = CircleShape) {
            Text(text = name)
        }
        Button(onClick = { }, shape = CircleShape) {
            Text(text = "Settings")
        }
        Button(onClick = { exitProcess(0) }, shape = CircleShape) {
            Text(text = "Exit")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    VirtualGamePadMobileTheme {
        MainMenu()
    }
}
