package io.github.kitswas.virtualgamepadmobile.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


@Composable
fun <T> ListItemPicker(
    modifier: Modifier = Modifier,
    list: Iterable<T>,
    default: T,
    label: String,
    onItemSelected: (T) -> Unit = { _ -> }
) {

    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier.wrapContentSize(Alignment.TopStart)
    ) {
        var selectedItem by remember {
            mutableStateOf(
                default
            )
        }
        OutlinedTextField(
            value = selectedItem.toString(),
            onValueChange = {},
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown,
                    "Click to see options",
                    modifier = Modifier.clickable { expanded = true })
            },
            readOnly = true,
            label = { Text(label) },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            list.forEach { item ->
                DropdownMenuItem(text = { Text(item.toString()) }, onClick = {
                    selectedItem = item
                    expanded = false
                    onItemSelected(item)
                })
            }
        }
    }
}

/**
 * Overloaded function to accept default index instead of default item
 */
@Composable
fun <T> ListItemPicker(
    modifier: Modifier = Modifier,
    list: Iterable<T>,
    default: Int = 0,
    label: String,
    onItemSelected: (T) -> Unit = { _ -> }
) {
    ListItemPicker(
        modifier = modifier,
        list = list,
        default = list.elementAt(default),
        label = label,
        onItemSelected = onItemSelected
    )
}
