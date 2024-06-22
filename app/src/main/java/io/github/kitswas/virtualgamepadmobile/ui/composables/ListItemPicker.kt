package io.github.kitswas.virtualgamepadmobile.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier


@Composable
@Suppress("unused")
fun <T> ListItemPicker(
    modifier: Modifier = Modifier,
    list: Iterable<T>,
    default: T,
    label: String,
    formattedDisplay: @Composable (T) -> Unit = { item ->
        Text(
            item.toString(), modifier = Modifier.width(IntrinsicSize.Max)
        )
    },
    onItemSelected: (T) -> Unit = { _ -> }
) {

    var expanded by remember { mutableStateOf(false) }

    Box(modifier) {
        var selectedItem = default
        Column {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            OutlinedButton(onClick = { expanded = true }) {
                Row {
                    formattedDisplay(selectedItem)
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Expand")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    list.forEach { item ->
                        DropdownMenuItem(text = { formattedDisplay(item) }, onClick = {
                            selectedItem = item
                            expanded = false
                            onItemSelected(item)
                        })
                    }
                }
            }
        }
    }
}

/**
 * Overloaded function to accept default index instead of default item
 */
@Composable
@Suppress("unused")
fun <T> ListItemPicker(
    modifier: Modifier = Modifier,
    list: Iterable<T>,
    default: Int = 0,
    label: String,
    formattedDisplay: @Composable (T) -> Unit = { item -> Text(item.toString()) },
    onItemSelected: (T) -> Unit = { _ -> }
) {
    ListItemPicker(
        modifier = modifier,
        list = list,
        default = list.elementAt(default),
        label = label,
        formattedDisplay = formattedDisplay,
        onItemSelected = onItemSelected
    )
}
