package io.github.kitswas.virtualgamepadmobile.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.kitswas.virtualgamepadmobile.ui.theme.Typography

@Composable
fun <T : Number> SpinBox(
    value: T,
    onValueChange: (T) -> Unit,
    label: String,
    minValue: T,
    maxValue: T,
    step: T,
    modifier: Modifier = Modifier,
    formatValue: (T) -> String = { it.toString() },
) {
    var currentValue by remember { mutableStateOf(value) }

    // Helper function to add numeric values - handles different numeric types
    fun add(a: T, b: T): T {
        @Suppress("UNCHECKED_CAST")
        return when (a) {
            is Int -> (a + (b as Int)) as T
            is Float -> (a + (b as Float)) as T
            is Double -> (a + (b as Double)) as T
            is Long -> (a + (b as Long)) as T
            else -> throw IllegalArgumentException("Unsupported numeric type")
        }
    }

    // Helper function to subtract numeric values - handles different numeric types
    fun subtract(a: T, b: T): T {
        @Suppress("UNCHECKED_CAST")
        return when (a) {
            is Int -> (a - (b as Int)) as T
            is Float -> (a - (b as Float)) as T
            is Double -> (a - (b as Double)) as T
            is Long -> (a - (b as Long)) as T
            else -> throw IllegalArgumentException("Unsupported numeric type")
        }
    }

    // Helper function to compare numeric values
    fun compare(a: T, b: T): Int {
        return a.toDouble().compareTo(b.toDouble())
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = Typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = {
                    if (compare(currentValue, minValue) > 0) {
                        val newValue = subtract(currentValue, step)
                        currentValue = if (compare(newValue, minValue) < 0) {
                            minValue
                        } else {
                            newValue
                        }
                        onValueChange(currentValue)
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Decrease"
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Surface(
                shape = MaterialTheme.shapes.small,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.width(60.dp)
            ) {
                Text(
                    text = formatValue(currentValue),
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = {
                    if (compare(currentValue, maxValue) < 0) {
                        val newValue = add(currentValue, step)
                        currentValue = if (compare(newValue, maxValue) > 0) {
                            maxValue
                        } else {
                            newValue
                        }
                        onValueChange(currentValue)
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Increase"
                )
            }
        }
    }
}
