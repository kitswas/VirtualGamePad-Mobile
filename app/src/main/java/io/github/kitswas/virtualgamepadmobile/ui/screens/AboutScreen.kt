package io.github.kitswas.virtualgamepadmobile.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp.Companion.Hairline
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.github.kitswas.virtualgamepadmobile.R
import io.github.kitswas.virtualgamepadmobile.data.PreviewBase
import io.github.kitswas.virtualgamepadmobile.data.PreviewHeightDp
import io.github.kitswas.virtualgamepadmobile.data.PreviewWidthDp

@Composable
fun AboutScreen(
    versionName: String? = LocalContext.current.packageManager.getPackageInfo(
        LocalContext.current.packageName, 0
    ).versionName,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // GitHub URLs
    val projectUrl = "https://kitswas.github.io/VirtualGamePad/"
    val mobileRepoUrl = "https://github.com/kitswas/VirtualGamePad-Mobile/"
    val mobileLicenseUrl = "https://github.com/kitswas/VirtualGamePad-Mobile/blob/main/LICENCE.TXT"
    val issuesUrl = "https://github.com/kitswas/VirtualGamePad-Mobile/issues/new"
    val releaseUrl = "https://github.com/kitswas/VirtualGamePad-Mobile/releases/latest"

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = stringResource(R.string.about_title),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.about_version, versionName ?: ""),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    modifier = Modifier.padding(0.dp),
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(Hairline, MaterialTheme.colorScheme.secondary),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, mobileLicenseUrl.toUri())
                        context.startActivity(intent)
                    }) {
                    Text(
                        stringResource(R.string.about_view_license),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                OutlinedButton(
                    modifier = Modifier.padding(0.dp),
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(Hairline, MaterialTheme.colorScheme.secondary),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, releaseUrl.toUri())
                        context.startActivity(intent)
                    }) {
                    Text(
                        stringResource(R.string.about_latest_release),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.about_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilledTonalButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, mobileRepoUrl.toUri())
                    context.startActivity(intent)
                }) {
                    Text(
                        stringResource(R.string.about_source_code),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                FilledTonalButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, projectUrl.toUri())
                    context.startActivity(intent)
                }) {
                    Text(
                        stringResource(R.string.about_project_website),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                FilledTonalButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, issuesUrl.toUri())
                    context.startActivity(intent)
                }) {
                    Text(
                        stringResource(R.string.about_report_issues),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onNavigateBack) {
                Text(stringResource(R.string.back))
            }
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
        AboutScreen(onNavigateBack = {}, versionName = stringResource(R.string.about_dev_version))
    }
}
