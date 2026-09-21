package io.github.kitswas.virtualgamepadmobile.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlin.math.pow

/**
 * Different analog stick feel profiles.
 *
 * Each mode shapes how physical stick deflection maps to the value sent to the
 * connected device. [sensitivity] scales the raw deflection before the response
 * curve is applied, so values > 1 reach full output with less travel and values
 * < 1 require more travel. [exponent] shapes the curve: exponents < 1 boost
 * small deflections (reacts sooner, fights client deadzones), exponents > 1
 * emphasize fine control near the center.
 */
@Serializable
@Parcelize
enum class StickResponseMode(
    val displayName: String,
    val exponent: Float,
    val sensitivity: Float,
) : Parcelable {
    STANDARD("Standard", 1.0f, 1.0f),
    PRECISE("Precise", 1.5f, 0.9f),
    RESPONSIVE("Responsive", 0.7f, 1.15f),
    BOOSTED("Boosted", 0.4f, 1.3f),
    TURBO("Turbo", 0.2f, 1.0f);

    /**
     * Maps a raw deflection ([deflection], normalized 0..1) to the output value.
     */
    fun map(deflection: Float): Float {
        val t = (deflection * sensitivity).coerceIn(0f, 1f)
        return t.pow(exponent)
    }

    companion object {
        val default = StickResponseMode.RESPONSIVE
    }
}