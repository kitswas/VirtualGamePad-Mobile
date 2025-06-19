package io.github.kitswas.virtualgamepadmobile.ui.utils

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlin.math.min

/**
 * Utility class for implementing rich haptic feedback patterns for gamepad controls
 */
object RichHapticUtils {
    private const val TAG = "RichHapticUtils"


    /**
     * Check if the device supports rich haptics
     */
    fun supportsRichHaptics(view: View): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false

        val vibrator =
            ContextCompat.getSystemService(view.context, Vibrator::class.java) ?: return false
        return vibrator.areAllPrimitivesSupported(
            VibrationEffect.Composition.PRIMITIVE_CLICK,
            VibrationEffect.Composition.PRIMITIVE_TICK,
            VibrationEffect.Composition.PRIMITIVE_LOW_TICK,
            VibrationEffect.Composition.PRIMITIVE_THUD,
        )
    }

    /**
     * Provides analog stick movement feedback with intensity based on stick displacement
     * @param view View to perform haptic feedback from
     * @param normalizedDistance Distance of thumb movement normalized between 0-1
     * @param isEdge Whether the stick is at its maximum edge position
     */
    fun performAnalogStickFeedback(view: View, normalizedDistance: Float, isEdge: Boolean) {
        if (!HapticUtils.isEnabled) return

        try {
            val vibrator =
                ContextCompat.getSystemService(view.context, Vibrator::class.java) ?: return

            // Check for primitive support
            if (supportsRichHaptics(view)) {

                // For edge feedback (when stick reaches maximum displacement)
                if (isEdge) {
                    performEdgeFeedback(vibrator)
                    return
                }

                // For regular movement, intensity varies based on displacement
                // Only trigger above certain threshold to avoid constant vibration
                if (normalizedDistance > 0.2f) {
                    val intensity = calculateIntensity(normalizedDistance)
                    performMovementFeedback(vibrator, intensity)
                }
            } else {
                // Fall back to basic haptic feedback for devices without composition support
                if (isEdge) {
                    HapticUtils.performGestureFeedback(view)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing rich haptic feedback: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun performEdgeFeedback(vibrator: Vibrator) {
        vibrator.vibrate(
            VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 1.0f)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 0.7f)
                .compose()
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun performMovementFeedback(vibrator: Vibrator, intensity: Float) {
        vibrator.vibrate(
            VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, intensity)
                .compose()
        )
    }

    /**
     * Calculate intensity for haptic feedback based on stick displacement
     * Map 0.4-1.0 range to 0.3-0.7 intensity range
     */
    private fun calculateIntensity(normalizedDistance: Float): Float {
        // Map the 0.4-1.0 range to 0.3-0.7 intensity range
        val mappedValue = (normalizedDistance - 0.4f) * (0.4f / 0.6f) + 0.3f
        return min(mappedValue, 0.7f)
    }
}
