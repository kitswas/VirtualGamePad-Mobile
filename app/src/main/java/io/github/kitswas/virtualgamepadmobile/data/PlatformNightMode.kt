package io.github.kitswas.virtualgamepadmobile.data

import android.app.UiModeManager
import android.content.Context
import android.os.Build

/**
 * Makes the platform resources follow the app's color scheme. The splash screen and the
 * window background are drawn by the system before the app can run, so they can only
 * follow the platform's night mode, which the platform keeps for the app until it is
 * changed or the app is uninstalled.
 */
fun applyPlatformNightMode(context: Context, colorScheme: ColorScheme) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return
    }
    val mode = when (colorScheme) {
        ColorScheme.DARK -> UiModeManager.MODE_NIGHT_YES
        ColorScheme.LIGHT -> UiModeManager.MODE_NIGHT_NO
        ColorScheme.SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
    }
    context.getSystemService(UiModeManager::class.java).setApplicationNightMode(mode)
}
