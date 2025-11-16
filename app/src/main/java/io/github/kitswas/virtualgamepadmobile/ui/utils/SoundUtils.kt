package io.github.kitswas.virtualgamepadmobile.ui.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import android.view.SoundEffectConstants
import android.view.View
import io.github.kitswas.virtualgamepadmobile.R
import io.github.kitswas.virtualgamepadmobile.data.defaultButtonClickSoundEnabled

object SoundUtils {
    private const val TAG = "SoundUtils"

    private var soundPool: SoundPool? = null
    private var pressSoundId: Int = 0
    private var releaseSoundId: Int = 0
    private var isLoaded: Boolean = false
    private var loadedCount: Int = 0

    var isEnabled: Boolean = defaultButtonClickSoundEnabled
        set(value) {
            field = value
            Log.d(TAG, "Button click sound enabled: $value")
            if (!value) {
                // Release soundpool when disabled to free resources
                try {
                    soundPool?.release()
                } catch (e: Exception) {
                    Log.e(TAG, "Error releasing SoundPool: ${e.message}")
                }
                soundPool = null
                pressSoundId = 0
                releaseSoundId = 0
                isLoaded = false
                loadedCount = 0
            }
        }

    fun performButtonPressSound(view: View) {
        if (!isEnabled) return
        try {
            ensureInitialized(view.context)
            if (isLoaded) {
                soundPool?.play(pressSoundId, 1f, 1f, 1, 0, 1f)
            } else {
                // fallback to system click while our sound loads
                view.playSoundEffect(SoundEffectConstants.CLICK)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing button press sound: ${e.message}")
        }
    }

    fun performButtonReleaseSound(view: View) {
        if (!isEnabled) return
        try {
            ensureInitialized(view.context)
            if (isLoaded) {
                soundPool?.play(releaseSoundId, 1f, 1f, 1, 0, 1f)
            } else {
                // fallback to system click while our sound loads
                view.playSoundEffect(SoundEffectConstants.CLICK)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing button release sound: ${e.message}")
        }
    }

    /**
     * Lazily initialize SoundPool and load resources.
     */
    private fun ensureInitialized(context: Context) {
        if (soundPool != null) return
        try {
            val audioAttr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(audioAttr)
                .build()

            // Reset loaded state
            isLoaded = false
            loadedCount = 0

            // Load our custom click sounds
            pressSoundId = soundPool?.load(context, R.raw.keypress, 1) ?: 0
            releaseSoundId = soundPool?.load(context, R.raw.keyrelease, 1) ?: 0

            soundPool?.setOnLoadCompleteListener { _, _, status ->
                if (status == 0) {
                    loadedCount++
                    if (loadedCount >= 2) isLoaded = true
                } else {
                    Log.e(TAG, "Failed to load sound resource with status $status")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing SoundPool: ${e.message}")
        }
    }

    /**
     * Public API to pre-load sounds and prepare SoundPool for low-latency playback.
     * This is safe to call multiple times and will no-op if SoundPool already initialized.
     */
    fun preload(context: Context) = ensureInitialized(context)

    fun release() {
        try {
            soundPool?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing SoundPool: ${e.message}")
        } finally {
            soundPool = null
            pressSoundId = 0
            releaseSoundId = 0
            isLoaded = false
            loadedCount = 0
        }
    }
}
