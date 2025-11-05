package io.github.kitswas.virtualgamepadmobile.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kitswas.virtualgamepadmobile.data.ButtonComponent
import io.github.kitswas.virtualgamepadmobile.data.ButtonConfig

@Composable
fun ButtonConfigEditor(
    component: ButtonComponent,
    config: ButtonConfig,
    onConfigChange: (ButtonConfig) -> Unit
) {
    var visible by remember(config) { mutableStateOf(config.visible) }
    var scale by remember(config) { mutableFloatStateOf(config.scale) }
    var offsetX by remember(config) { mutableFloatStateOf(config.offsetX) }
    var offsetY by remember(config) { mutableFloatStateOf(config.offsetY) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Component name and visibility toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = component.displayName,
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (visible) "Visible" else "Hidden",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Switch(
                        checked = visible,
                        onCheckedChange = {
                            visible = it
                            onConfigChange(config.copy(visible = it))
                        }
                    )
                }
            }

            // Scale slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scale: ${String.format("%.2f", scale)}x",
                    style = MaterialTheme.typography.bodyMedium
                )
                androidx.compose.material3.Slider(
                    value = scale,
                    onValueChange = { scale = it },
                    onValueChangeFinished = {
                        onConfigChange(config.copy(scale = scale))
                    },
                    valueRange = 0.5f..1.5f,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
            }

            // Offset X slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Offset X: ${String.format("%.0f", offsetX)}dp",
                    style = MaterialTheme.typography.bodyMedium
                )
                androidx.compose.material3.Slider(
                    value = offsetX,
                    onValueChange = { offsetX = it },
                    onValueChangeFinished = {
                        onConfigChange(config.copy(offsetX = offsetX))
                    },
                    valueRange = -100f..100f,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
            }

            // Offset Y slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Offset Y: ${String.format("%.0f", offsetY)}dp",
                    style = MaterialTheme.typography.bodyMedium
                )
                androidx.compose.material3.Slider(
                    value = offsetY,
                    onValueChange = { offsetY = it },
                    onValueChangeFinished = {
                        onConfigChange(config.copy(offsetY = offsetY))
                    },
                    valueRange = -100f..100f,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
            }
        }
    }
}
