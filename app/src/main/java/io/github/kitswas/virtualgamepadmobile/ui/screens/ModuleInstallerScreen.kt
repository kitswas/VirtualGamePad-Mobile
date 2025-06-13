package io.github.kitswas.virtualgamepadmobile.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kitswas.virtualgamepadmobile.data.PreviewBase
import io.github.kitswas.virtualgamepadmobile.data.PreviewHeightDp
import io.github.kitswas.virtualgamepadmobile.data.PreviewWidthDp
import io.github.kitswas.virtualgamepadmobile.ui.theme.Typography
import io.github.kitswas.virtualgamepadmobile.utils.QRScannerManager
import io.github.kitswas.virtualgamepadmobile.utils.QRScannerManagerInterface

@Composable
fun ModuleInstallerScreen(
    onNavigateBack: () -> Unit,
    onInstallationComplete: () -> Unit,
    qrScannerManager: QRScannerManagerInterface
) {
    var installationState by remember { mutableStateOf(InstallationState.WAITING) }
    var installationProgress by remember { mutableFloatStateOf(0f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Monitor module availability changes
    val moduleAvailability by qrScannerManager.moduleAvailabilityState.collectAsState()

    LaunchedEffect(moduleAvailability) {
        when (moduleAvailability) {
            QRScannerManager.ModuleAvailability.AVAILABLE -> {
                Log.d("ModuleInstallerScreen", "Modules became available, completing installation")
                onInstallationComplete()
            }

            QRScannerManager.ModuleAvailability.INSTALL_FAILED -> {
                installationState = InstallationState.FAILED
                errorMessage = "Module installation failed"
            }

            QRScannerManager.ModuleAvailability.INSTALL_CANCELLED -> {
                installationState = InstallationState.CANCELLED
                errorMessage = "Installation was cancelled"
            }

            else -> {
                // Other states are handled by the UI based on installationState
            }
        }
    }

    BackHandler {
        if (installationState == InstallationState.INSTALLING) {
            // Cancel installation if in progress
            qrScannerManager.cancelInstallation()
        }
        onNavigateBack()
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "QR Scanner Module Required",
                style = Typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            val annotatedString = buildAnnotatedString {
                append("The QR code scanner requires additional modules to be downloaded.\n")
                append("The module is a part of Google Play Services.\n")
                append("See ")

                val link =
                    "https://developers.google.com/ml-kit/vision/barcode-scanning/code-scanner"
                withLink(
                    LinkAnnotation.Url(
                        link,
                        TextLinkStyles(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.secondary,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    )
                ) {
                    append(link)
                }

                append("\nThis is a one-time setup that enables QR scanning functionality.")
            }

            Text(
                text = annotatedString,
                style = Typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (installationState) {
                InstallationState.WAITING -> {
                    Button(
                        onClick = {
                            installationState = InstallationState.INSTALLING
                            qrScannerManager.startInstallation(
                                onProgress = { progress ->
                                    installationProgress = progress / 100f
                                },
                                onComplete = {
                                    installationState = InstallationState.COMPLETED
                                },
                                onError = { error ->
                                    installationState = InstallationState.FAILED
                                    errorMessage = error
                                }
                            )
                        }
                    ) {
                        Text("Download QR Scanner Module")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = onNavigateBack) {
                        Text("Cancel")
                    }
                }

                InstallationState.INSTALLING -> {
                    CircularProgressIndicator()

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Downloading module...",
                        style = Typography.bodyMedium
                    )

                    if (installationProgress > 0f) {
                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { installationProgress },
                            modifier = Modifier.width(200.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${(installationProgress * 100).toInt()}%",
                            style = Typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = {
                            qrScannerManager.cancelInstallation()
                            onNavigateBack()
                        }
                    ) {
                        Text("Cancel")
                    }
                }

                InstallationState.COMPLETED -> {
                    Text(
                        text = "✓ Installation completed successfully!",
                        style = Typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = onInstallationComplete) {
                        Text("Continue")
                    }
                }

                InstallationState.FAILED, InstallationState.CANCELLED -> {
                    Text(
                        text = errorMessage ?: "Installation failed",
                        style = Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            installationState = InstallationState.WAITING
                            errorMessage = null
                        }
                    ) {
                        Text("Try Again")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = onNavigateBack) {
                        Text("Cancel")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Note: Please stay connected to the internet during download.",
                style = Typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private enum class InstallationState {
    WAITING,
    INSTALLING,
    COMPLETED,
    FAILED,
    CANCELLED
}

@Preview(
    widthDp = PreviewWidthDp,
    heightDp = PreviewHeightDp,
)
@Composable
fun ModuleInstallerScreenPreview() {
    PreviewBase {
        ModuleInstallerScreen(
            onNavigateBack = {},
            onInstallationComplete = {},
            qrScannerManager = QRScannerManager(LocalContext.current)
        )
    }
}
