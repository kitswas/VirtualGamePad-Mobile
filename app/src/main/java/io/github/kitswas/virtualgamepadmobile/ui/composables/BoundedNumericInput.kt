package io.github.kitswas.virtualgamepadmobile.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp


@Composable
fun <T : Number> BoundedNumericInput(
    value: T,
    onValueChange: (T) -> Unit,
    label: String,
    minValue: T,
    maxValue: T,
    parseValue: (String) -> T?,
    formatValue: (T) -> String = { it.toString() },
    keyboardType: KeyboardType = KeyboardType.Number,
    compareValues: (T, T) -> Int = { a, b -> a.toDouble().compareTo(b.toDouble()) },
    modifier: Modifier = Modifier
) {
    var textValue by remember { mutableStateOf(formatValue(value)) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                textValue = newValue
                try {
                    val parsedValue = parseValue(newValue)
                    if (parsedValue != null) {
                        when {
                            compareValues(parsedValue, minValue) < 0 -> {
                                isError = true
                                errorMessage = "Value must be at least ${formatValue(minValue)}"
                            }

                            compareValues(parsedValue, maxValue) > 0 -> {
                                isError = true
                                errorMessage = "Value must be at most ${formatValue(maxValue)}"
                            }

                            else -> {
                                isError = false
                                errorMessage = ""
                                onValueChange(parsedValue)
                            }
                        }
                    } else {
                        isError = true
                        errorMessage = "Please enter a valid number"
                    }
                } catch (_: Exception) {
                    isError = true
                    errorMessage = "Please enter a valid number"
                }
            },
            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = isError,
            supportingText = {
                if (isError) {
                    Text(errorMessage)
                }
            },
            modifier = Modifier.width(120.dp)
        )
    }
}
