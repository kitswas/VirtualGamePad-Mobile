package io.github.kitswas.virtualgamepadmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.window.layout.WindowMetricsCalculator
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import io.github.kitswas.virtualgamepadmobile.ui.theme.VirtualGamePadMobileTheme

class MainActivity : ComponentActivity() {

    private lateinit var scanner: GmsBarcodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepareQRScanner()

        setContent {
            val metrics = WindowMetricsCalculator.getOrCreate()
                .computeCurrentWindowMetrics(this)

            val widthDp = metrics.bounds.width() /
                    resources.displayMetrics.density
            val heightDp = metrics.bounds.height() /
                    resources.displayMetrics.density

            VirtualGamePadMobileTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Holder(widthDp, heightDp)
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
    fun Holder(
        widthDp: Float,
        heightDp: Float,
    ) {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "main_menu") {
            composable("main_menu") {
                MainMenu(navController)
            }
            composable("connect_screen") {
                ConnectMenu(navController, scanner)
            }
            composable("gamepad") {
                GamePad(widthDp, heightDp)
            }
        }
    }

    @Composable
    @Preview(showBackground = true)
    fun DefaultPreview() {
        val metrics = WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(this)

        val widthDp = metrics.bounds.width() /
                resources.displayMetrics.density
        val heightDp = metrics.bounds.height() /
                resources.displayMetrics.density
        VirtualGamePadMobileTheme {
            Holder(widthDp, heightDp)
        }
    }
}
