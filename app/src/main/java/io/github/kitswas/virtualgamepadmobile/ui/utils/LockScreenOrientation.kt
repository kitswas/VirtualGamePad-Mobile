package io.github.kitswas.virtualgamepadmobile.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext


/**
 * Locks the screen orientation to the given orientation.
 *
 * Set-only, deliberately NO restore on dispose: every screen declares its own
 * orientation, and restoring would yank the *next* screen — during navigation
 * transitions the exiting screen disposes after the entering screen composes,
 * so a restore fires ~1s late and flips the gamepad back to portrait,
 * piling its widgets on each other. The activity-level destination listener
 * in MainActivity is the authority; this is the backup.
 * @see <a href="https://stackoverflow.com/a/69231996/8659747"> StackOverflow Answer <a/>
 */
@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(orientation) {
        context.findActivity()?.requestedOrientation = orientation
        onDispose {}
    }
}
