package io.github.kitswas.virtualgamepadmobile.ui.screens

import android.content.Context
import android.annotation.SuppressLint
import android.util.Log
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.R
import io.github.kitswas.virtualgamepadmobile.data.ButtonAnchor
import io.github.kitswas.virtualgamepadmobile.data.ButtonComponent
import io.github.kitswas.virtualgamepadmobile.data.ButtonConfig
import io.github.kitswas.virtualgamepadmobile.data.CustomProfileStorage
import io.github.kitswas.virtualgamepadmobile.data.SCALE_VALUE_RANGE
import io.github.kitswas.virtualgamepadmobile.data.SettingsRepository
import io.github.kitswas.virtualgamepadmobile.data.StickResponseMode
import io.github.kitswas.virtualgamepadmobile.data.defaultButtonConfigs
import io.github.kitswas.virtualgamepadmobile.ui.composables.*
import io.github.kitswas.virtualgamepadmobile.ui.theme.PristineWhite
import io.github.kitswas.virtualgamepadmobile.ui.utils.LockScreenOrientation
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

private fun ButtonAnchor.toAlignment(): Alignment = when (this) {
    ButtonAnchor.TOP_LEFT -> Alignment.TopStart
    ButtonAnchor.TOP_CENTER -> Alignment.TopCenter
    ButtonAnchor.TOP_RIGHT -> Alignment.TopEnd
    ButtonAnchor.CENTER_LEFT -> Alignment.CenterStart
    ButtonAnchor.CENTER -> Alignment.Center
    ButtonAnchor.CENTER_RIGHT -> Alignment.CenterEnd
    ButtonAnchor.BOTTOM_LEFT -> Alignment.BottomStart
    ButtonAnchor.BOTTOM_CENTER -> Alignment.BottomCenter
    ButtonAnchor.BOTTOM_RIGHT -> Alignment.BottomEnd
}

private fun sanitizeButtonConfigs(configs: Map<ButtonComponent, ButtonConfig>): Map<ButtonComponent, ButtonConfig> {
    return ButtonComponent.entries.associateWith { component ->
        configs[component] ?: ButtonConfig.default(component)
    }
}

internal fun shouldPromptToSaveOnExit(
    isNewProfile: Boolean,
    editableConfigs: Map<ButtonComponent, ButtonConfig>,
    baselineConfigs: Map<ButtonComponent, ButtonConfig>
): Boolean = isNewProfile || editableConfigs != baselineConfigs

/**
 * Calculates dynamic boundary limits based on the current anchor and screen size.
 */
private fun getDragBounds(
    anchor: ButtonAnchor,
    screenWidthDp: Int,
    screenHeightDp: Int,
    baseDp: Int
): Pair<ClosedFloatingPointRange<Float>, ClosedFloatingPointRange<Float>> {
    val deadZone = baseDp / 18
    val innerWidth = screenWidthDp - (deadZone * 2)
    val innerHeight = screenHeightDp - (deadZone * 2)

    val ratioX = innerWidth / baseDp.toFloat()
    val ratioY = innerHeight / baseDp.toFloat()

    val buffer = 0.15f

    val xRange = when (anchor) {
        ButtonAnchor.TOP_LEFT, ButtonAnchor.CENTER_LEFT, ButtonAnchor.BOTTOM_LEFT -> -buffer..(ratioX + buffer)
        ButtonAnchor.TOP_CENTER, ButtonAnchor.CENTER, ButtonAnchor.BOTTOM_CENTER -> -(ratioX / 2 + buffer)..(ratioX / 2 + buffer)
        ButtonAnchor.TOP_RIGHT, ButtonAnchor.CENTER_RIGHT, ButtonAnchor.BOTTOM_RIGHT -> -(ratioX + buffer)..buffer
    }

    val yRange = when (anchor) {
        ButtonAnchor.TOP_LEFT, ButtonAnchor.TOP_CENTER, ButtonAnchor.TOP_RIGHT -> -buffer..(ratioY + buffer)
        ButtonAnchor.CENTER_LEFT, ButtonAnchor.CENTER, ButtonAnchor.CENTER_RIGHT -> -(ratioY / 2 + buffer)..(ratioY / 2 + buffer)
        ButtonAnchor.BOTTOM_LEFT, ButtonAnchor.BOTTOM_CENTER, ButtonAnchor.BOTTOM_RIGHT -> -(ratioY + buffer)..buffer
    }

    return Pair(xRange, yRange)
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun GamepadCustomizationScreen(
    onNavigateBack: () -> Unit,
    settingsRepository: SettingsRepository,
    profileId: String? = null,
    isNewProfile: Boolean = false,
    onProfileSaved: (() -> Unit)? = null
) {
    // Layout editor needs landscape like the gamepad itself. FIXED landscape:
    // any rotation while the canvas is up piles the widgets on each other.
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current.density
    val context = LocalContext.current
    val profileStorage = remember { CustomProfileStorage(context) }
    val editingProfileId = profileId?.takeIf { it.isNotBlank() }

    // State Tracking
    val settingsConfigs by settingsRepository.buttonConfigs.collectAsState(initial = null)
    var isLoaded by remember { mutableStateOf(false) }
    var baselineConfigs by remember { mutableStateOf(defaultButtonConfigs) }
    var editableConfigs by remember { mutableStateOf(defaultButtonConfigs) }

    var selectedComponent by remember { mutableStateOf<ButtonComponent?>(null) }
    var showAddMenu by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showAnchorMenu by remember { mutableStateOf(false) }
    var showResponseMenu by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var pillOffsetX by remember { mutableFloatStateOf(0f) }
    var pillOffsetY by remember { mutableFloatStateOf(0f) }

    // When editing an existing profile, load its saved config directly from storage so
    // edits are based on the stored layout (not whatever happens to be active).
    LaunchedEffect(editingProfileId) {
        if (editingProfileId != null) {
            val loaded = profileStorage.loadProfileConfigs(editingProfileId)
            baselineConfigs = loaded ?: defaultButtonConfigs
            editableConfigs = baselineConfigs
            isLoaded = true
        }
    }

    LaunchedEffect(settingsConfigs, isNewProfile) {
        if (editingProfileId == null && settingsConfigs != null) {
            baselineConfigs = if (isNewProfile) defaultButtonConfigs else settingsConfigs!!
            editableConfigs = baselineConfigs
            isLoaded = true
        }
    }

    LaunchedEffect(isNewProfile) {
        if (isNewProfile) {
            selectedComponent = null
            showAddMenu = false
            showSettingsMenu = false
            showExportDialog = false
            showImportDialog = false
            showExitDialog = false
            showResponseMenu = false
            importJsonText = ""
            pillOffsetX = 0f
            pillOffsetY = 0f
            isLoaded = true
        }
    }

    val dummyState = remember { GamepadReading() }

    val saveAndExit = {
        scope.launch {
            if (editingProfileId != null) {
                val existingName = profileStorage.listProfiles()
                    .find { it.id == editingProfileId }?.name ?: context.getString(R.string.main_custom_layout)
                profileStorage.saveProfile(editingProfileId, existingName, editableConfigs)
                settingsRepository.setActiveProfileId(editingProfileId)
                onProfileSaved?.invoke()
            } else if (isNewProfile) {
                val newProfileId = "custom_${System.currentTimeMillis()}"
                val newProfileName = context.getString(R.string.main_new_profile, profileStorage.listProfiles().size + 1)
                Log.d("CustomProfile", "Saving new profile $newProfileId ($newProfileName)")
                profileStorage.saveProfile(newProfileId, newProfileName, editableConfigs)
                settingsRepository.setActiveProfileId(newProfileId)
                Log.d("CustomProfile", "Saved profile file count=${profileStorage.listProfiles().size}")
                onProfileSaved?.invoke()
            }

            settingsRepository.setAllButtonConfigs(editableConfigs)

            showExitDialog = false
            onNavigateBack()
        }
    }

    val attemptExit = {
        if (shouldPromptToSaveOnExit(isNewProfile, editableConfigs, baselineConfigs)) {
            showExitDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler { attemptExit() }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Save Changes?", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("You have unsaved changes to this layout. Would you like to save them before exiting?") },
            confirmButton = {
                Button(
                    onClick = { saveAndExit() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                ) {
                    Text("Save Layout", color = PristineWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false; onNavigateBack() }) {
                    Text("Discard", color = Color.Red)
                }
            }
        )
    }

    if (showExportDialog) {
        ExportConfigDialog(buttonConfigs = editableConfigs, onDismiss = { showExportDialog = false })
    }
    if (showImportDialog) {
        ImportConfigDialog(
            importedJsonText = importJsonText,
            onJsonTextChange = { importJsonText = it },
            onImport = { configs ->
                editableConfigs = configs
                showImportDialog = false
                importJsonText = ""
            },
            onDismiss = { showImportDialog = false; importJsonText = "" }
        )
    }

    if (!isLoaded) return

    // Wrapping everything in BoxWithConstraints so it uses the EXACT physical drawing space available
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { selectedComponent = null })
            }
    ) {
        // Now baseDp is guaranteed to match DrawGamepad.kt perfectly
        val screenWidthDp = maxWidth.value.toInt()
        val screenHeightDp = maxHeight.value.toInt()
        val baseDp = screenHeightDp 
        val deadZonePadding = baseDp / 18

        // EDITOR CANVAS
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(deadZonePadding.dp)
        ) {
            ButtonComponent.entries.forEach { component ->
                val config = editableConfigs[component] ?: ButtonConfig.default(component)
                
                if (config.visible) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = config.anchor.toAlignment()
                    ) {
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = (config.offsetX * baseDp).dp,
                                    y = (config.offsetY * baseDp).dp
                                )
                        ) {
                            Box(modifier = Modifier.align(Alignment.Center)) {
                                when (component) {
                                    ButtonComponent.LEFT_ANALOG_STICK -> AnalogStick(outerCircleWidth = (baseDp / 8 * config.scale).dp, innerCircleRadius = (baseDp / 12 * config.scale).dp, knobScale = config.analogInnerScale, type = AnalogStickType.LEFT, gamepadState = dummyState)
                                    ButtonComponent.RIGHT_ANALOG_STICK -> AnalogStick(outerCircleWidth = (baseDp / 8 * config.scale).dp, innerCircleRadius = (baseDp / 12 * config.scale).dp, knobScale = config.analogInnerScale, type = AnalogStickType.RIGHT, gamepadState = dummyState)
                                    ButtonComponent.DPAD -> Dpad(size = (0.45 * baseDp * config.scale).dp, gamepadState = dummyState)
                                    ButtonComponent.FACE_BUTTONS -> FaceButtons(size = (0.45 * baseDp * config.scale).dp, gamepadState = dummyState)
                                    ButtonComponent.LEFT_TRIGGER -> Trigger(type = TriggerType.LEFT, size = (baseDp / 6 * config.scale).dp, gamepadState = dummyState)
                                    ButtonComponent.RIGHT_TRIGGER -> Trigger(type = TriggerType.RIGHT, size = (baseDp / 6 * config.scale).dp, gamepadState = dummyState)
                                    ButtonComponent.LEFT_SHOULDER -> ShoulderButton(type = ShoulderButtonType.LEFT, size = (baseDp / 8 * config.scale).dp, gamepadState = dummyState)
                                    ButtonComponent.RIGHT_SHOULDER -> ShoulderButton(type = ShoulderButtonType.RIGHT, size = (baseDp / 8 * config.scale).dp, gamepadState = dummyState)
                                    ButtonComponent.SELECT_BUTTON -> MenuButton(type = MenuButtonType.VIEW, size = (baseDp / 8 * config.scale).dp, gamepadState = dummyState)
                                    ButtonComponent.START_BUTTON -> MenuButton(type = MenuButtonType.MENU, size = (baseDp / 8 * config.scale).dp, gamepadState = dummyState)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .border(
                                        width = if (selectedComponent == component) 1.dp else 0.dp,
                                        color = if (selectedComponent == component) Color.Yellow else Color.Transparent
                                    )
                                    .pointerInput(Unit) {
                                        detectTapGestures(onTap = { selectedComponent = component })
                                    }
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = { selectedComponent = component },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                val dx = (dragAmount.x / density) / baseDp
                                                val dy = (dragAmount.y / density) / baseDp
                                                val currentConfig = editableConfigs[component] ?: ButtonConfig.default(component)
                                                
                                                val (xBounds, yBounds) = getDragBounds(
                                                    anchor = currentConfig.anchor,
                                                    screenWidthDp = screenWidthDp,
                                                    screenHeightDp = screenHeightDp,
                                                    baseDp = baseDp
                                                )
                                                
                                                editableConfigs = editableConfigs.toMutableMap().apply {
                                                    put(component, currentConfig.copy(
                                                        offsetX = (currentConfig.offsetX + dx).coerceIn(xBounds),
                                                        offsetY = (currentConfig.offsetY + dy).coerceIn(yBounds)
                                                    ))
                                                }
                                            }
                                        )
                                    }
                            )
                        }
                    }
                }
            }
        } // End of Padded Canvas

        // Draggable Space-Saving Control Pill 
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset { IntOffset(pillOffsetX.roundToInt(), pillOffsetY.roundToInt()) }
                .widthIn(min = 200.dp, max = 320.dp)
                .padding(top = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xEE990000))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag Handle
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to move panel",
                    tint = PristineWhite.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                pillOffsetX += dragAmount.x
                                pillOffsetY += dragAmount.y
                            }
                        }
                )

                if (selectedComponent != null) {
                    val currentConfig = editableConfigs[selectedComponent]!!
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Custom width/height", color = PristineWhite, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.size(16.dp).border(1.dp, PristineWhite))
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = currentConfig.scale,
                            onValueChange = { newScale ->
                                editableConfigs = editableConfigs.toMutableMap().apply {
                                    put(selectedComponent!!, currentConfig.copy(scale = newScale))
                                }
                            },
                            valueRange = 0.5f..2.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = PristineWhite,
                                activeTrackColor = Color(0x88FFFFFF),
                                inactiveTrackColor = Color(0x44FFFFFF)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Size", color = PristineWhite, style = MaterialTheme.typography.bodySmall)
                            Text("${(currentConfig.scale * 100).toInt()}%", color = PristineWhite, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedComponent == ButtonComponent.LEFT_ANALOG_STICK ||
                        selectedComponent == ButtonComponent.RIGHT_ANALOG_STICK
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Slider(
                                value = currentConfig.analogInnerScale,
                                onValueChange = { newInnerScale ->
                                    editableConfigs = editableConfigs.toMutableMap().apply {
                                        put(selectedComponent!!, currentConfig.copy(analogInnerScale = newInnerScale))
                                    }
                                },
                                valueRange = SCALE_VALUE_RANGE,
                                colors = SliderDefaults.colors(
                                    thumbColor = PristineWhite,
                                    activeTrackColor = Color(0x88FFFFFF),
                                    inactiveTrackColor = Color(0x44FFFFFF)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Knob", color = PristineWhite, style = MaterialTheme.typography.bodySmall)
                                Text("${(currentConfig.analogInnerScale * 100).toInt()}%", color = PristineWhite, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Per-stick response curve, saved to this profile.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Response", color = PristineWhite, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.weight(1f))
                            Box {
                                TextButton(onClick = { showResponseMenu = true }) {
                                    Text(currentConfig.responseMode.displayName, color = PristineWhite)
                                }
                                DropdownMenu(
                                    expanded = showResponseMenu,
                                    onDismissRequest = { showResponseMenu = false }
                                ) {
                                    StickResponseMode.entries.forEach { mode ->
                                        DropdownMenuItem(
                                            text = { Text(mode.displayName) },
                                            onClick = {
                                                editableConfigs = editableConfigs.toMutableMap().apply {
                                                    put(selectedComponent!!, currentConfig.copy(responseMode = mode))
                                                }
                                                showResponseMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Gyro", color = PristineWhite, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(
                                checked = currentConfig.gyro,
                                onCheckedChange = { enabled ->
                                    editableConfigs = editableConfigs.toMutableMap().apply {
                                        put(selectedComponent!!, currentConfig.copy(gyro = enabled))
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PristineWhite,
                                    checkedTrackColor = Color(0x88388E3C),
                                    uncheckedThumbColor = Color(0x88FFFFFF),
                                    uncheckedTrackColor = Color(0x44FFFFFF)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete, 
                            contentDescription = "Delete", 
                            tint = PristineWhite,
                            modifier = Modifier.clickable {
                                editableConfigs = editableConfigs.toMutableMap().apply {
                                    put(selectedComponent!!, currentConfig.copy(visible = false))
                                }
                                selectedComponent = null
                            }
                        )
                        Icon(
                            imageVector = Icons.Default.Refresh, 
                            contentDescription = "Reset", 
                            tint = PristineWhite,
                            modifier = Modifier.clickable {
                                editableConfigs = editableConfigs.toMutableMap().apply {
                                    put(selectedComponent!!, ButtonConfig.default(selectedComponent!!))
                                }
                            }
                        )
                        Box {
                            IconButton(onClick = { showAnchorMenu = true }) {
                                Icon(imageVector = Icons.Default.Lens, contentDescription = "Anchor", tint = PristineWhite)
                            }
                            DropdownMenu(expanded = showAnchorMenu, onDismissRequest = { showAnchorMenu = false }) {
                                ButtonAnchor.entries.forEach { anchor ->
                                    DropdownMenuItem(
                                        text = { Text(anchor.displayName) },
                                        onClick = {
                                            val configToUpdate = editableConfigs[selectedComponent] ?: ButtonConfig.default(selectedComponent!!)
                                            editableConfigs = editableConfigs.toMutableMap().apply {
                                                put(selectedComponent!!, configToUpdate.copy(anchor = anchor, offsetX = 0f, offsetY = 0f))
                                            }
                                            showAnchorMenu = false
                                        }
                                    )
                                }
                            }
                        }
                        Icon(imageVector = Icons.Default.Palette, contentDescription = "Color", tint = PristineWhite)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
                }

                // Compact Base Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        IconButton(onClick = { showSettingsMenu = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PristineWhite)
                        }
                        DropdownMenu(expanded = showSettingsMenu, onDismissRequest = { showSettingsMenu = false }) {
                            DropdownMenuItem(text = { Text("Export Profile") }, onClick = { showSettingsMenu = false; showExportDialog = true })
                            DropdownMenuItem(text = { Text("Import Profile") }, onClick = { showSettingsMenu = false; showImportDialog = true })
                            DropdownMenuItem(
                                text = { Text("Reset to Defaults", color = Color.Red) }, 
                                onClick = { 
                                    editableConfigs = defaultButtonConfigs
                                    selectedComponent = null
                                    showSettingsMenu = false 
                                }
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { showAddMenu = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Item", tint = PristineWhite)
                        }
                        DropdownMenu(expanded = showAddMenu, onDismissRequest = { showAddMenu = false }) {
                            val hiddenItems = ButtonComponent.entries.filter { editableConfigs[it]?.visible != true }
                            if (hiddenItems.isEmpty()) {
                                DropdownMenuItem(text = { Text("All items are visible") }, onClick = { showAddMenu = false })
                            } else {
                                hiddenItems.forEach { hiddenComponent ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(hiddenComponent.nameRes)) },
                                        onClick = {
                                            editableConfigs = editableConfigs.toMutableMap().apply {
                                                put(hiddenComponent, (editableConfigs[hiddenComponent] ?: ButtonConfig.default(hiddenComponent)).copy(visible = true))
                                            }
                                            selectedComponent = hiddenComponent
                                            showAddMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    IconButton(onClick = {
                        if (isNewProfile) {
                            saveAndExit()
                        } else {
                            attemptExit()
                        }
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Exit", tint = PristineWhite)
                    }
                }
            }
        }
    }
}
// (ExportConfigDialog and ImportConfigDialog remain below this exactly as they were...)


@Composable
fun ExportConfigDialog(
    buttonConfigs: Map<ButtonComponent, ButtonConfig>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    val jsonString = remember(buttonConfigs) {
        Json.encodeToString(buttonConfigs)
    }
    val exportLabel = stringResource(R.string.customization_export_label)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.customization_export_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    stringResource(R.string.customization_export_desc),
                    style = MaterialTheme.typography.bodySmall
                )
                TextField(
                    value = jsonString,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    label = { Text(stringResource(R.string.customization_export_label)) },
                    readOnly = true,
                    singleLine = false,
                    maxLines = 5,
                    textStyle = MaterialTheme.typography.bodySmall,
                    trailingIcon = {
                        Button(onClick = {
                            val clip = android.content.ClipData.newPlainText(exportLabel, jsonString)
                            clipboard.setPrimaryClip(clip)
                        }) {
                            Text(stringResource(R.string.customization_copy))
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text(stringResource(R.string.customization_done)) }
        }
    )
}

@Composable
fun ImportConfigDialog(
    importedJsonText: String,
    onJsonTextChange: (String) -> Unit,
    onImport: (Map<ButtonComponent, ButtonConfig>) -> Unit,
    onDismiss: () -> Unit
) {
    var hasError by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.customization_import_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    stringResource(R.string.customization_import_desc),
                    style = MaterialTheme.typography.bodySmall
                )
                if (hasError) {
                    Text(
                        stringResource(R.string.customization_import_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                TextField(
                    value = importedJsonText,
                    onValueChange = onJsonTextChange,
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    placeholder = { Text(stringResource(R.string.customization_import_placeholder)) },
                    singleLine = false,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val configs: Map<ButtonComponent, ButtonConfig> = Json.decodeFromString(importedJsonText)
                        val sanitizedConfigs = sanitizeButtonConfigs(configs)
                        hasError = false
                        onImport(sanitizedConfigs)
                    } catch (e: Exception) {
                        hasError = true
                    }
                }
            ) { Text(stringResource(R.string.customization_import)) }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
