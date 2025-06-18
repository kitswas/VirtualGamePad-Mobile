package io.github.kitswas.virtualgamepadmobile.ui.utils

import android.os.Build
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Utility class for handling haptic feedback consistently across the app
 */
object HapticUtils {
    fun performButtonPressFeedback(view: View) {
        try {
            view.performHapticFeedback(
                HapticFeedbackConstants.VIRTUAL_KEY,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
            )
        } catch (e: Exception) {
            // Log the error but don't crash if haptic feedback fails
            Log.e("HapticUtils", "Error performing haptic feedback: ${e.message}")
        }
    }

    fun performButtonReleaseFeedback(view: View) {
        try {
            // For Android 8.1 (API 27) and higher, use VIRTUAL_KEY_RELEASE
            if (Build.VERSION.SDK_INT >= 27) {
                view.performHapticFeedback(
                    HapticFeedbackConstants.VIRTUAL_KEY_RELEASE,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                )
            } else {
                // For older versions, fall back to VIRTUAL_KEY
                view.performHapticFeedback(
                    HapticFeedbackConstants.VIRTUAL_KEY,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                )
            }
        } catch (e: Exception) {
            // Log the error but don't crash if haptic feedback fails
            Log.e("HapticUtils", "Error performing haptic feedback: ${e.message}")
        }
    }

    fun performGestureFeedback(view: View) {
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                view.performHapticFeedback(
                    HapticFeedbackConstants.GESTURE_START,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                )
            }
        } catch (e: Exception) {
            // Log the error but don't crash if haptic feedback fails
            Log.e("HapticUtils", "Error performing gesture haptic feedback: ${e.message}")
        }
    }

    fun performGestureEndFeedback(view: View) {
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                view.performHapticFeedback(
                    HapticFeedbackConstants.GESTURE_END,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                )
            }
        } catch (e: Exception) {
            // Log the error but don't crash if haptic feedback fails
            Log.e("HapticUtils", "Error performing gesture end haptic feedback: ${e.message}")
        }
    }
}
