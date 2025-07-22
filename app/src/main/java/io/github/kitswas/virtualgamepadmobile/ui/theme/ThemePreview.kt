package io.github.kitswas.virtualgamepadmobile.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kitswas.virtualgamepadmobile.data.BaseColor
import io.github.kitswas.virtualgamepadmobile.data.ColorScheme

@Composable
private fun VirtualGamePadMobileThemePreview(
    darkMode: ColorScheme = ColorScheme.SYSTEM,
    baseColor: BaseColor = BaseColor.BLUE
) {
    VirtualGamePadMobileTheme(
        darkMode = darkMode,
        baseColor = baseColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Theme Color Palette",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            // Primary Colors Section
            ColorSection(
                title = "Primary Colors",
                colors = listOf(
                    ColorInfo("Primary", MaterialTheme.colorScheme.primary),
                    ColorInfo("On Primary", MaterialTheme.colorScheme.onPrimary),
                    ColorInfo("Primary Container", MaterialTheme.colorScheme.primaryContainer),
                    ColorInfo("On Primary Container", MaterialTheme.colorScheme.onPrimaryContainer)
                )
            )

            // Secondary Colors Section
            ColorSection(
                title = "Secondary Colors",
                colors = listOf(
                    ColorInfo("Secondary", MaterialTheme.colorScheme.secondary),
                    ColorInfo("On Secondary", MaterialTheme.colorScheme.onSecondary),
                    ColorInfo("Secondary Container", MaterialTheme.colorScheme.secondaryContainer),
                    ColorInfo(
                        "On Secondary Container",
                        MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            )

            // Tertiary Colors Section
            ColorSection(
                title = "Tertiary Colors",
                colors = listOf(
                    ColorInfo("Tertiary", MaterialTheme.colorScheme.tertiary),
                    ColorInfo("On Tertiary", MaterialTheme.colorScheme.onTertiary),
                    ColorInfo("Tertiary Container", MaterialTheme.colorScheme.tertiaryContainer),
                    ColorInfo(
                        "On Tertiary Container",
                        MaterialTheme.colorScheme.onTertiaryContainer
                    )
                )
            )

            // Surface Colors Section
            ColorSection(
                title = "Surface Colors",
                colors = listOf(
                    ColorInfo("Surface", MaterialTheme.colorScheme.surface),
                    ColorInfo("On Surface", MaterialTheme.colorScheme.onSurface),
                    ColorInfo("Surface Variant", MaterialTheme.colorScheme.surfaceVariant),
                    ColorInfo("On Surface Variant", MaterialTheme.colorScheme.onSurfaceVariant)
                )
            )

            // Background Colors Section
            ColorSection(
                title = "Background Colors",
                colors = listOf(
                    ColorInfo("Background", MaterialTheme.colorScheme.background),
                    ColorInfo("On Background", MaterialTheme.colorScheme.onBackground)
                )
            )

            // Error Colors Section
            ColorSection(
                title = "Error Colors",
                colors = listOf(
                    ColorInfo("Error", MaterialTheme.colorScheme.error),
                    ColorInfo("On Error", MaterialTheme.colorScheme.onError),
                    ColorInfo("Error Container", MaterialTheme.colorScheme.errorContainer),
                    ColorInfo("On Error Container", MaterialTheme.colorScheme.onErrorContainer)
                )
            )

            // Other Colors Section
            ColorSection(
                title = "Other Colors",
                colors = listOf(
                    ColorInfo("Outline", MaterialTheme.colorScheme.outline),
                    ColorInfo("Outline Variant", MaterialTheme.colorScheme.outlineVariant),
                    ColorInfo("Scrim", MaterialTheme.colorScheme.scrim),
                    ColorInfo("Inverse Surface", MaterialTheme.colorScheme.inverseSurface),
                    ColorInfo("Inverse On Surface", MaterialTheme.colorScheme.inverseOnSurface),
                    ColorInfo("Inverse Primary", MaterialTheme.colorScheme.inversePrimary)
                )
            )
        }
    }
}

@Composable
private fun ColorSection(
    title: String,
    colors: List<ColorInfo>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            colors.chunked(2).forEach { rowColors ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowColors.forEach { colorInfo ->
                        ColorSwatch(
                            colorInfo = colorInfo,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill remaining space if odd number of colors
                    if (rowColors.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    colorInfo: ColorInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = colorInfo.color,
                        shape = RoundedCornerShape(6.dp)
                    )
            )

            Text(
                text = colorInfo.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 10.sp
            )

            Text(
                text = "#${Integer.toHexString(colorInfo.color.toArgb()).uppercase().substring(2)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 8.sp
            )
        }
    }
}

private data class ColorInfo(
    val name: String,
    val color: Color
)

@Composable
@PreviewLightDark()
private fun ThemeDefault() {
    VirtualGamePadMobileThemePreview()
}
