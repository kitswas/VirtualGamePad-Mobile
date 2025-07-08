package io.github.kitswas.virtualgamepadmobile.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

sealed class QRScanResult {
    data class Success(val content: String) : QRScanResult()
    data class Error(val message: String) : QRScanResult()
    object Cancelled : QRScanResult()
    object PermissionDenied : QRScanResult()
}

@Composable
fun rememberQRCodeScanner(
    onResult: (QRScanResult) -> Unit
): () -> Unit {
    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        when {
            result.contents != null -> {
                onResult(QRScanResult.Success(result.contents))
            }

            else -> {
                onResult(QRScanResult.Cancelled)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, start scanning
            val scanOptions = ScanOptions()
            scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            scanOptions.setPrompt("Scan QR Code shown on the server")
            scanOptions.setCameraId(0)
            scanOptions.setBeepEnabled(false)
            scanOptions.setBarcodeImageEnabled(false)
            scanLauncher.launch(scanOptions)
        } else {
            onResult(QRScanResult.PermissionDenied)
        }
    }

    return {
        // Start by requesting camera permission
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
}
