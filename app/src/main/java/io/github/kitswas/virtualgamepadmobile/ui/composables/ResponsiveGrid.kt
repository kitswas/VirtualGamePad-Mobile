package io.github.kitswas.virtualgamepadmobile.ui.composables

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times

const val LogTag = "ResponsiveGrid"

/**
 * A responsive grid composable that automatically calculates the number of columns
 * based on available width and minimum item width. Supports incremental rendering
 * by batching items to minimize layout recalculation.
 *
 * @param items List of items to display in the grid
 * @param minItemWidth Minimum width each item should have (default 320.dp)
 * @param horizontalSpacing Spacing between columns (default 8.dp)
 * @param verticalSpacing Spacing between rows (default 8.dp)
 * @param batchSize Number of items to render in each batch. If null, defaults to column count.
 *                  Use this to control incremental rendering - useful with animated reveals.
 * @param batchDelayMs Delay in milliseconds between rendering each batch (default 200ms).
 *                     Set to 0 to render all items immediately.
 * @param minItems Minimum number of items to display instantly before batching starts (default 0).
 *                 Useful to ensure some items are visible immediately.
 * @param modifier Modifier to apply to the grid container
 * @param content Composable lambda to render each item
 */
@Composable
fun <T> ResponsiveGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    minItemWidth: Dp = 320.dp,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    batchSize: Int? = null,
    batchDelayMs: Long = 200L,
    minItems: Int = 0,
    content: @Composable (item: T) -> Unit
) {
    // Calculate available width for content
    // LocalWindowInfo returns size in pixels, convert to dp
    val screenWidthPx = LocalWindowInfo.current.containerSize.width
    val density = androidx.compose.ui.platform.LocalDensity.current
    val screenWidth = with(density) { screenWidthPx.toDp() }

    // Calculate number of columns based on screen width
    // Account for horizontal padding on each side
    val availableWidth = screenWidth - (2 * horizontalSpacing)
    val itemWidthWithSpacing = minItemWidth + horizontalSpacing
    val columns = maxOf(1, (availableWidth / itemWidthWithSpacing).toInt())

    Log.d(
        LogTag,
        "screenWidth=$screenWidth, availableWidth=$availableWidth, itemWidthWithSpacing=$itemWidthWithSpacing, columns=$columns"
    )

    // Use provided batch size or default to column count
    val actualBatchSize = batchSize ?: columns

    // Incrementally load items with 200ms delays between batches
    var renderedItems by remember { mutableStateOf<List<T>>(emptyList()) }

    LaunchedEffect(items) {
        // Display minimum items instantly
        val minItemsCount = minItems.coerceAtMost(items.size)
        renderedItems = if (minItemsCount > 0) {
            items.take(minItemsCount)
        } else {
            emptyList()
        }

        // Then batch load remaining items with delays
        var index = minItemsCount
        while (index < items.size) {
            if (batchDelayMs > 0) {
                kotlinx.coroutines.delay(batchDelayMs)
            }
            val batchEnd = (index + actualBatchSize).coerceAtMost(items.size)
            renderedItems = items.take(batchEnd)
            index = batchEnd
        }
    }

    // Batch items into grid rows
    val rows = renderedItems.chunked(columns)

    Log.d(LogTag, "Rendering grid with $columns columns and ${rows.size} rows.")

    rows.forEach { rowItems ->
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = verticalSpacing),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
        ) {
            rowItems.forEach { item ->
                Box(modifier = Modifier.weight(1f)) {
                    content(item)
                }
            }

            // Add spacers for incomplete rows
            repeat(columns - rowItems.size) {
                Box(modifier = Modifier.weight(1f))
            }
        }
    }
}
