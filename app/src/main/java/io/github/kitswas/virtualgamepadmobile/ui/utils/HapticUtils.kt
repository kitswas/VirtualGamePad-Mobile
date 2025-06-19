package io.github.kitswas.virtualgamepadmobile.ui.utils

import android.os.Build
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import io.github.kitswas.virtualgamepadmobile.data.defaultHapticFeedbackEnabled

/**
 * Utility class for handling haptic feedback consistently across the app.
 * Follows Android's haptics design principles:
 * - Uses HapticFeedbackConstants for consistency across the system
 * - Avoids problematic one-shot vibrations and buzzy patterns
 * - Works on low-end devices
 */
object HapticUtils {
    private const val TAG = "HapticUtils"

    // Override flags to ensure haptic feedback is always triggered if enabled
    private const val OVERRIDE_FLAGS = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING or
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING

    // Static property to control haptic feedback globally
    var isEnabled: Boolean = defaultHapticFeedbackEnabled
        set(value) {
            field = value
            Log.d(TAG, "Haptic feedback enabled: $value")
        }

    /**
     * Provides haptic feedback for gamepad button press events (buttons down).
     * Uses VIRTUAL_KEY constant which provides a clear, crisp button press sensation
     * that's consistent with Android system behavior.
     */
    fun performButtonPressFeedback(view: View) {
        if (!isEnabled) return
        try {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, OVERRIDE_FLAGS)
        } catch (e: Exception) {
            Log.e(TAG, "Error performing button press haptic feedback: ${e.message}")
        }
    }

    /**
     * Provides haptic feedback for gamepad button release events (buttons up).
     * Uses VIRTUAL_KEY_RELEASE (API 27+) for a softer release feel, with fallback to basic feedback.
     * This creates a complete press-release cycle that feels natural.
     */
    fun performButtonReleaseFeedback(view: View) {
        if (!isEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                view.performHapticFeedback(
                    HapticFeedbackConstants.VIRTUAL_KEY_RELEASE, OVERRIDE_FLAGS
                )
            } else {
                // For older versions, use a very light alternative
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, OVERRIDE_FLAGS)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing button release haptic feedback: ${e.message}")
        }
    }

    /**
     * Provides haptic feedback when analog stick is released and returns to center.
     * Uses GESTURE_END (API 30+) for modern devices, with fallback to subtle feedback.
     * This signals the completion of the analog input gesture.
     */
    fun performGestureEndFeedback(view: View) {
        if (!isEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END, OVERRIDE_FLAGS)
            } else {
                // For older devices, use lighter feedback
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, OVERRIDE_FLAGS)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing gesture end haptic feedback: ${e.message}")
        }
    }

    /**
     * Provides subtle haptic feedback for analog stick movement at discrete thresholds.
     * Only triggers when reaching significant displacement to avoid constant vibration.
     * Uses CLOCK_TICK for a very light touch sensation that doesn't overwhelm.
     *
     * @param normalizedDistance Distance from center (0-1 range)
     */
    fun performAnalogMovementFeedback(view: View, normalizedDistance: Float) {
        if (!isEnabled) return

        // Following haptics principles: correlate frequency with subtlety
        // Only provide feedback at significant movement thresholds to avoid annoyance
        when {
            normalizedDistance >= 0.95f -> {
                // At edge tick
                try {
                    // Use CLOCK_TICK for subtle movement feedback
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK, OVERRIDE_FLAGS)
                } catch (e: Exception) {
                    Log.e(TAG, "Error performing analog movement haptic feedback: ${e.message}")
                }
            }
            // No feedback for lower movement to avoid constant buzzing
        }
    }
}
