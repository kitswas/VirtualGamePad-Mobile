package io.github.kitswas.virtualgamepadmobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun Circle(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier.clip(shape = CircleShape).background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier,
            contentAlignment,
            propagateMinConstraints,
        ) {
            content()
        }
    }
}

@Preview(showBackground = false)
@Composable
fun CirclePreview() {
    Circle(
        modifier = Modifier
            .background(Color.Red)
            .size(16.dp),
        contentAlignment = Alignment.Center
    ) {}
}
