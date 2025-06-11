package io.github.kitswas.virtualgamepadmobile.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.github.kitswas.virtualgamepadmobile.data.PreviewBase
import io.github.kitswas.virtualgamepadmobile.data.PreviewHeightDp
import io.github.kitswas.virtualgamepadmobile.data.PreviewWidthDp
import io.github.kitswas.virtualgamepadmobile.ui.composables.Badge
import io.github.kitswas.virtualgamepadmobile.ui.theme.Typography

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // GitHub URLs
    val projectUrl = "https://kitswas.github.io/VirtualGamePad/"
    val repoUrl = "https://github.com/kitswas/VirtualGamePad/"
    val mobileRepoUrl = "https://github.com/kitswas/VirtualGamePad-Mobile/"
    val mobileLicenseUrl = "https://github.com/kitswas/VirtualGamePad-Mobile/blob/main/LICENCE.TXT"
    val issuesUrl = "https://github.com/kitswas/VirtualGamePad-Mobile/issues/new"
    val releaseUrl = "https://github.com/kitswas/VirtualGamePad-Mobile/releases/latest"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Virtual GamePad Mobile",
            style = Typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Version ${
                context.packageManager.getPackageInfo(
                    context.packageName,
                    0
                ).versionName
            }",
            style = Typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Badges row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Badge(
                imageUrl = "https://raster.shields.io/github/license/kitswas/VirtualGamePad-Mobile",
                linkUrl = mobileLicenseUrl,
                contentDescription = "License Badge"
            )

            Badge(
                imageUrl = "https://raster.shields.io/github/stars/kitswas/VirtualGamePad?style=social",
                linkUrl = repoUrl,
                contentDescription = "GitHub Stars Badge"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Badge(
                imageUrl = "https://raster.shields.io/github/downloads/kitswas/VirtualGamePad-Mobile/total",
                linkUrl = releaseUrl,
                contentDescription = "Downloads Badge"
            )

            Badge(
                imageUrl = "https://raster.shields.io/github/v/release/kitswas/VirtualGamePad-Mobile?logo=github",
                linkUrl = releaseUrl,
                contentDescription = "Release Badge"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "A mobile application that lets your phone function as a gamepad controller for PC games.",
            style = Typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, projectUrl.toUri())
                context.startActivity(intent)
            }) {
                Text("Project Website")
            }

            TextButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, mobileRepoUrl.toUri())
                context.startActivity(intent)
            }) {
                Text("Source Code")
            }

            TextButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, issuesUrl.toUri())
                context.startActivity(intent)
            }) {
                Text("Report Issues")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onNavigateBack) {
            Text("Back")
        }
    }
}

@Preview(
    widthDp = PreviewWidthDp,
    heightDp = PreviewHeightDp,
)
@Composable
fun AboutScreenPreview() {
    PreviewBase {
        AboutScreen(onNavigateBack = {})
    }
}
