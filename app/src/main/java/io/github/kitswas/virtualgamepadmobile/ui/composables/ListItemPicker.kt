package io.github.kitswas.virtualgamepadmobile.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.ui.res.painterResource
import io.github.kitswas.virtualgamepadmobile.R
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * A dropdown list picker for selecting an item from a list.
 * @param modifier The modifier to be applied to the layout.
 * @param list List of items to choose from.
 * @param default Default item to display.
 * @param label Label to display above the picker.
 * @param formattedDisplay A composable function to format and display the item.
 */
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

    var selectedItem = default
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        OutlinedButton(onClick = { expanded = true }) {
            Row {
                formattedDisplay(selectedItem)
                Icon(painter = painterResource(R.drawable.ic_arrow_drop_down), contentDescription = "Expand")
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

/**
 * Overloaded function to accept default index instead of default item.
 * @see ListItemPicker
 */
@Composable
@Suppress("unused")
fun <T> ListItemPicker(
    modifier: Modifier = Modifier,
    list: Iterable<T>,
    defaultIndex: Int = 0,
    label: String,
    formattedDisplay: @Composable (T) -> Unit = { item -> Text(item.toString()) },
    onItemSelected: (T) -> Unit = { _ -> }
) {
    ListItemPicker(
        modifier = modifier,
        list = list,
        default = list.elementAt(defaultIndex),
        label = label,
        formattedDisplay = formattedDisplay,
        onItemSelected = onItemSelected
    )
}