package io.github.kitswas.virtualgamepadmobile

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Locks the screen orientation to the given orientation.
 * @see <a href="https://stackoverflow.com/a/69231996/8659747"> StackOverflow Answer <a/>
 */
@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(orientation) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            // restore original orientation when view disappears
            activity.requestedOrientation = originalOrientation
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun GamePad(widthDp: Float, heightDp: Float) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

//    // Scale the gamepad to fit the screen
//    val configuration = LocalConfiguration.current
//    val baseDp = when (configuration.orientation) {
//        Configuration.ORIENTATION_LANDSCAPE -> {
//            heightDp
//        }
//
//        else -> {
//            widthDp
//        }
//    }
    val baseDp = heightDp // Assuming Landscape orientation

    // First we make a box that will contain the gamepad
    // And put padding around it so that it doesn't touch the edges of the screen
    Box(
        modifier = Modifier
            .padding((baseDp / 18).dp)
//            .background(Color.Magenta)
            .fillMaxSize()
    ) {
    }
}

const val PreviewWidthDp = 900
const val PreviewHeightDp = 400

@Preview(
    showBackground = true,
    widthDp = PreviewWidthDp,
    heightDp = PreviewHeightDp,
)
@Composable
fun GamePadPreview() {
    GamePad(PreviewWidthDp.toFloat(), PreviewHeightDp.toFloat())
}

@Preview(
    showBackground = true,
    widthDp = PreviewWidthDp,
    heightDp = PreviewHeightDp,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun GamePadPreviewNight() {
    GamePad(PreviewWidthDp.toFloat(), PreviewHeightDp.toFloat())
}
