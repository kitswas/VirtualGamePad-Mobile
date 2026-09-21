package io.github.kitswas.virtualgamepadmobile.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import io.github.kitswas.virtualgamepadmobile.R
import io.github.kitswas.virtualgamepadmobile.data.CustomProfileStorage
import io.github.kitswas.virtualgamepadmobile.ui.composables.ResponsiveGrid
import io.github.kitswas.virtualgamepadmobile.ui.theme.DeepSurface
import io.github.kitswas.virtualgamepadmobile.ui.theme.PristineWhite
import io.github.kitswas.virtualgamepadmobile.ui.theme.PristineWhite
import io.github.kitswas.virtualgamepadmobile.ui.theme.SlateGray
import io.github.kitswas.virtualgamepadmobile.ui.utils.LockScreenOrientation

data class GamepadProfile(
    val id: String,
    val name: String,
    val isCustom: Boolean = false
)

@Composable
fun MainMenu(
    isConnected: Boolean = false,
    onConnectClick: () -> Unit,
    onProfileSelect: (GamepadProfile) -> Unit,
    onCreateCustomProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onEditCustomProfile: (GamepadProfile) -> Unit,
    refreshKey: Int = 0
) {
    // FORCE PORTRAIT MODE FOR DASHBOARD
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    var currentTab by rememberSaveable { mutableIntStateOf(2) } 
    
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val profileStorage = remember { CustomProfileStorage(context) }
    var customProfiles by remember { mutableStateOf(listOf<GamepadProfile>()) }
    val defaultProfileName = stringResource(R.string.main_default_profile)
    val unifiedProfiles = remember(customProfiles, defaultProfileName) {
        listOf(GamepadProfile("game_controller", defaultProfileName, false)) + customProfiles
    }

    val refreshCustomProfiles = {
        customProfiles = profileStorage.listProfiles().map {
            GamepadProfile(it.id, it.name, true)
        }
    }

    // Bulletproof database reload triggered every time the screen becomes active
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshCustomProfiles()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        refreshCustomProfiles()

        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(refreshKey) {
        refreshCustomProfiles()
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xCC101218),
                tonalElevation = 0.dp,
                modifier = Modifier.border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0; onNavigateToSettings() },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.menu_settings), style = MaterialTheme.typography.bodySmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PristineWhite,
                        unselectedIconColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1; onNavigateToAbout() },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text(stringResource(R.string.menu_about), style = MaterialTheme.typography.bodySmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PristineWhite,
                        unselectedIconColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.menu_home), style = MaterialTheme.typography.bodySmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PristineWhite,
                        indicatorColor = Color(0xFF2E2E2E),
                        unselectedIconColor = Color.Gray
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0E0E0E))
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF151515))
                    .border(1.dp, Color(0xFF262626), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.main_title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = PristineWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 26.sp,
                                letterSpacing = 0.2.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isConnected) stringResource(R.string.main_connected) else stringResource(R.string.main_disconnected),
                            color = if (isConnected) PristineWhite else Color(0xFF9CA3AF),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Box(
                        modifier = Modifier
                            .shadow(8.dp, RoundedCornerShape(999.dp), clip = false)
                            .clip(RoundedCornerShape(999.dp))
                            .background(PristineWhite)
                    ) {
                        Button(
                            onClick = onConnectClick,
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color(0xFF0E0E0E)
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                            elevation = null,
                            modifier = Modifier.defaultMinSize(minHeight = 0.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color(0xFF0E0E0E),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isConnected) stringResource(R.string.main_connected) else stringResource(R.string.connect_button),
                                    color = Color(0xFF0E0E0E),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                if (unifiedProfiles.isEmpty()) {
                    Text(
                        text = stringResource(R.string.main_no_layouts),
                        color = Color(0xFF9CA3AF),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    ResponsiveGrid(
                        items = unifiedProfiles,
                        minItemWidth = 160.dp,
                        horizontalSpacing = 12.dp,
                        verticalSpacing = 12.dp
                    ) { profile ->
                        ProfileCard(
                            profile = profile,
                            onProfileSelect = onProfileSelect,
                            showDefaultLabel = profile.id == "game_controller",
                            onEdit = if (profile.isCustom) { { onEditCustomProfile(profile) } } else null,
                            onDelete = if (profile.isCustom) {
                                {
                                    profileStorage.deleteProfile(profile.id)
                                    refreshCustomProfiles()
                                }
                            } else null,
                            onRename = if (profile.isCustom) { newName ->
                                val finalName = newName.trim().ifBlank { profile.name }
                                profileStorage.renameProfile(profile.id, finalName)
                                refreshCustomProfiles()
                            } else null,
                            onShare = if (profile.isCustom) {
                                {
                                    profileStorage.shareProfile(profile.id)?.let { content ->
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, content)
                                            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.main_share_subject, profile.name))
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.main_share_title)))
                                    }
                                }
                            } else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { onCreateCustomProfile() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0x66FF2A4D)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PristineWhite,
                        containerColor = Color(0x14FFFFFF)
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(text = stringResource(R.string.main_new_layout), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileCard(
    profile: GamepadProfile,
    onProfileSelect: (GamepadProfile) -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onRename: ((String) -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    showDefaultLabel: Boolean = false
) {
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember(profile.id) { mutableStateOf(false) }
    val renameFieldState = remember(profile.id) { mutableStateOf(TextFieldValue(profile.name)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProfileSelect(profile) }
            .shadow(16.dp, RoundedCornerShape(16.dp), clip = false),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E26)),
        border = BorderStroke(1.dp, Color(0x0FFFFFFF))
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF15161B))
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(14.dp))
                        .padding(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1D1F2A))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    )

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF101118))
                            .border(1.dp, Color(0xFF2B2F3C), CircleShape)
                            .align(Alignment.CenterStart)
                    )

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1C1F2C))
                            .border(1.dp, Color(0xFF3A3E4F), CircleShape)
                            .align(Alignment.Center)
                    )

                    Column(
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFF2A4D)))
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFF2A4D)))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFF2A4D)))
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFF2A4D)))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF2A4D))
                            .align(Alignment.BottomCenter)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
) {
    
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = profile.name,
            color = PristineWhite,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (showDefaultLabel) {
            Text(
                text = stringResource(R.string.main_default_badge),
                color = Color(0xFF9CA3AF),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }

    if (profile.isCustom) {
        Box(
            modifier = Modifier
                .padding(end = 4.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0x3315161B))
                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(999.dp))
        ) {
            
            IconButton(
                onClick = { showOptionsMenu = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.MoreVert, 
                    contentDescription = null, 
                    tint = PristineWhite,
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = showOptionsMenu,
                onDismissRequest = { showOptionsMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.profile_update)) },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    onClick = {
                        showOptionsMenu = false
                        onEdit?.invoke()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.profile_rename)) },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    onClick = {
                        showOptionsMenu = false
                        renameFieldState.value = TextFieldValue(profile.name, selection = TextRange(0, profile.name.length))
                        showRenameDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.profile_share)) },
                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                    onClick = {
                        showOptionsMenu = false
                        onShare?.invoke()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.profile_delete)) },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    onClick = {
                        showOptionsMenu = false
                        onDelete?.invoke()
                    }
                )
            }
        }
    }
}
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text(stringResource(R.string.main_rename_title)) },
            text = {
                OutlinedTextField(
                    value = renameFieldState.value,
                    onValueChange = { renameFieldState.value = it },
                    label = { Text(stringResource(R.string.main_rename_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val finalName = renameFieldState.value.text.trim()
                    if (finalName.isNotEmpty()) {
                        onRename?.invoke(finalName)
                    }
                    showRenameDialog = false
                }) {
                    Text(stringResource(R.string.save),color=PristineWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text(stringResource(R.string.cancel), color = Color(0xFF6B6B6B))
                }
            }
        )
    }
}
