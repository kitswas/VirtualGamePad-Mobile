package io.github.kitswas.virtualgamepadmobile.ui.composables

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

private const val TAG = "Badge"

/**
 * Displays a shields.io badge that opens a URL when clicked
 *
 * @param imageUrl URL of the badge image from shields.io
 * @param linkUrl URL to open when the badge is clicked
 * @param contentDescription Accessibility description for the badge
 * @param modifier Modifier for the badge
 */
@Composable
fun Badge(
    imageUrl: String,
    linkUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, linkUrl.toUri())
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to open URL: $linkUrl", e)
                }
            }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .height(20.dp)
                .width(120.dp)
        )
    }
}
