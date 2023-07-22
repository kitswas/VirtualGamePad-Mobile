package io.github.kitswas.virtualgamepadmobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.kitswas.virtualgamepadmobile.ui.theme.darken
import io.github.kitswas.virtualgamepadmobile.ui.theme.lighten

@Composable
fun AnalogStick(
    ringColor: Color = MaterialTheme.colorScheme.outline,
    ringWidth: Dp = 2.dp,
    outerCircleColor: Color = lighten(MaterialTheme.colorScheme.primary, 0.2f),
    outerCircleWidth: Dp = 4.dp,
    innerCircleColor: Color = darken(MaterialTheme.colorScheme.primary, 0.2f),
    innerCircleRadius: Dp = 32.dp,
) {
    // First draw the glow ring
    Circle(
        modifier = Modifier
            .background(ringColor)
            .size((innerCircleRadius + outerCircleWidth + ringWidth) * 2),
        contentAlignment = Alignment.Center
    ) {
        // Then draw the outer circle
        Circle(
            modifier = Modifier
                .background(outerCircleColor)
                .size((innerCircleRadius + outerCircleWidth) * 2),
            contentAlignment = Alignment.Center
        ) {
            // Then draw the inner circle
            Circle(
                modifier = Modifier
                    .background(innerCircleColor)
                    .size(innerCircleRadius * 2)
            ) {

            }
        }
    }
}

@Preview(showBackground = false)
@Composable
fun AnalogStickPreview() {
    AnalogStick()
}
