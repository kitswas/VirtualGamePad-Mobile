package io.github.kitswas.virtualgamepadmobile.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices.DESKTOP
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.R
import io.github.kitswas.virtualgamepadmobile.data.MotionStickControl
import io.github.kitswas.virtualgamepadmobile.data.PreviewBase
import io.github.kitswas.virtualgamepadmobile.data.PreviewHeightDp
import io.github.kitswas.virtualgamepadmobile.data.PreviewWidthDp
import io.github.kitswas.virtualgamepadmobile.data.SettingsRepository
import io.github.kitswas.virtualgamepadmobile.data.defaultButtonConfigs
import io.github.kitswas.virtualgamepadmobile.data.defaultMotionSensitivity
import io.github.kitswas.virtualgamepadmobile.data.defaultMotionStickControl
import io.github.kitswas.virtualgamepadmobile.data.defaultPollingDelay
import io.github.kitswas.virtualgamepadmobile.network.ConnectionViewModel
import io.github.kitswas.virtualgamepadmobile.ui.composables.DrawGamepad
import io.github.kitswas.virtualgamepadmobile.ui.utils.findActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI

private const val tag = "GamePadScreen"
private const val maxTiltDegrees = 30f
private const val gravityAlpha = 0.8f

private fun toStickValue(angleRad: Float, sensitivity: Float): Float {
    val maxTiltRadians = (maxTiltDegrees / 180f * PI).toFloat()
    return (angleRad / maxTiltRadians * sensitivity).coerceIn(-1f, 1f)
}

private fun toStickValueFromGravity(gravityComponent: Float, sensitivity: Float): Float {
    val normalized = (gravityComponent / SensorManager.GRAVITY_EARTH).coerceIn(-1f, 1f)
    return (normalized * sensitivity).coerceIn(-1f, 1f)
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun GamePad(
    connectionViewModel: ConnectionViewModel?,
    onNavigateBack: () -> Unit,
) {
    val gamepadState by rememberSaveable { mutableStateOf(GamepadReading()) }
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    val pollingDelay =
        settingsRepository.pollingDelay.collectAsState(defaultPollingDelay).value.toLong()
    val buttonConfigs =
        settingsRepository.buttonConfigs.collectAsState(defaultButtonConfigs).value
    val motionStickControl =
        settingsRepository.motionStickControl.collectAsState(defaultMotionStickControl).value
    val motionSensitivity =
        settingsRepository.motionSensitivity.collectAsState(defaultMotionSensitivity).value

    val motionStickX = remember { mutableStateOf(0f) }
    val motionStickY = remember { mutableStateOf(0f) }

    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp
    val screenWidth = configuration.screenWidthDp

    val isStopping = remember { mutableStateOf(false) }

    DrawGamepad(
        widthDp = screenWidth,
        heightDp = screenHeight,
        gamepadState = gamepadState,
        buttonConfigs = buttonConfigs,
        motionStickControl = motionStickControl,
        motionStickX = motionStickX.value,
        motionStickY = motionStickY.value,
    )

    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val motionSensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }
    val displayRotation =
        LocalContext.current.findActivity()?.display?.rotation ?: Surface.ROTATION_0

    DisposableEffect(motionStickControl, motionSensitivity, motionSensor, displayRotation) {
        if (motionStickControl == MotionStickControl.OFF || motionSensor == null) {
            motionStickX.value = 0f
            motionStickY.value = 0f
            if (motionStickControl == MotionStickControl.LEFT) {
                gamepadState.LeftThumbstickX = 0f
                gamepadState.LeftThumbstickY = 0f
            }
            if (motionStickControl == MotionStickControl.RIGHT) {
                gamepadState.RightThumbstickX = 0f
                gamepadState.RightThumbstickY = 0f
            }
            return@DisposableEffect onDispose { }
        }

        val rotationMatrix = FloatArray(9)
        val remappedRotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        val gravity = FloatArray(3)

        val axisConfig = when (displayRotation) {
            Surface.ROTATION_90 -> SensorManager.AXIS_Z to SensorManager.AXIS_MINUS_X
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Z
            Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Z to SensorManager.AXIS_X
            else -> SensorManager.AXIS_X to SensorManager.AXIS_Z
        }

        fun applyStickValues(stickX: Float, stickY: Float) {
            motionStickX.value = stickX
            motionStickY.value = stickY
            when (motionStickControl) {
                MotionStickControl.LEFT -> {
                    gamepadState.LeftThumbstickX = stickX
                    gamepadState.LeftThumbstickY = stickY
                }

                MotionStickControl.RIGHT -> {
                    gamepadState.RightThumbstickX = stickX
                    gamepadState.RightThumbstickY = stickY
                }

                MotionStickControl.OFF -> Unit
            }
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    gravity[0] = gravityAlpha * gravity[0] + (1f - gravityAlpha) * event.values[0]
                    gravity[1] = gravityAlpha * gravity[1] + (1f - gravityAlpha) * event.values[1]

                    val (rawX, rawY) = when (displayRotation) {
                        Surface.ROTATION_90 -> gravity[1] to gravity[0]
                        Surface.ROTATION_180 -> gravity[0] to -gravity[1]
                        Surface.ROTATION_270 -> -gravity[1] to -gravity[0]
                        else -> -gravity[0] to gravity[1]
                    }
                    applyStickValues(
                        stickX = toStickValueFromGravity(rawX, motionSensitivity),
                        stickY = toStickValueFromGravity(rawY, motionSensitivity),
                    )
                    return
                }

                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    axisConfig.first,
                    axisConfig.second,
                    remappedRotationMatrix,
                )
                SensorManager.getOrientation(remappedRotationMatrix, orientation)

                val pitch = orientation[1]
                val roll = orientation[2]
                applyStickValues(
                    stickX = toStickValue(-roll, motionSensitivity),
                    stickY = toStickValue(pitch, motionSensitivity),
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(listener, motionSensor, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager.unregisterListener(listener)
            motionStickX.value = 0f
            motionStickY.value = 0f
            if (motionStickControl == MotionStickControl.LEFT) {
                gamepadState.LeftThumbstickX = 0f
                gamepadState.LeftThumbstickY = 0f
            }
            if (motionStickControl == MotionStickControl.RIGHT) {
                gamepadState.RightThumbstickX = 0f
                gamepadState.RightThumbstickY = 0f
            }
        }
    }

    val activity = LocalContext.current.findActivity()
    // disconnect on back press
    androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
        .addObserver(androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY
                && activity?.isChangingConfigurations != true // ignore screen rotation
            ) {
                if (connectionViewModel != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            isStopping.value = true
                            // unset all keys before disconnecting
                            gamepadState.ButtonsUp = gamepadState.ButtonsDown
                            gamepadState.ButtonsDown = 0
                            gamepadState.LeftThumbstickX = 0F
                            gamepadState.LeftThumbstickY = 0F
                            gamepadState.RightThumbstickX = 0F
                            gamepadState.RightThumbstickY = 0F
                            gamepadState.LeftTrigger = 0F
                            gamepadState.RightTrigger = 0F
                            connectionViewModel.enqueueGamepadState(gamepadState)
                            connectionViewModel.disconnect()
                            Log.d(tag, "Disconnected")
                        } catch (e: Exception) {
                            Log.d(tag, "Error during disconnect: ${e.message}")
                        }
                    }
                }
            }
        })

    // Monitor connection state and handle errors
    val connectionState by connectionViewModel?.uiState?.collectAsState()
        ?: remember { mutableStateOf(null) }

    val connectionLostMessage = connectionState?.takeIf { !it.connected }?.let { state ->
        state.error?.let {
            stringResource(R.string.gamepad_connection_lost_error, it)
        } ?: stringResource(R.string.gamepad_connection_lost)
    }

    LaunchedEffect(connectionLostMessage) {
        if (connectionLostMessage != null) {
            Log.d(tag, connectionLostMessage)
            Toast.makeText(context, connectionLostMessage, Toast.LENGTH_LONG).show()
            onNavigateBack()
        }
    }

    val startAfter = 100L // in milliseconds

    // Send gamepad state updates periodically
    LaunchedEffect(gamepadState, pollingDelay) {
        delay(startAfter)

        // Start sending updates
        while (connectionViewModel != null && !isStopping.value) {
            // Queue the update in the ViewModel
            connectionViewModel.enqueueGamepadState(gamepadState)

            // Reset ButtonsUp after each update
            gamepadState.ButtonsUp = 0

            // Wait before next update
            delay(pollingDelay)
        }
    }
}

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(
    name = "Design Preview (Light)",
    device = "spec:width=${PreviewWidthDp}dp,height=${PreviewHeightDp}dp,orientation=landscape,dpi=420",
)
@Preview(
    name = "Design Preview (Dark)",
    device = "spec:width=${PreviewWidthDp}dp,height=${PreviewHeightDp}dp,orientation=landscape,dpi=420",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Preview(
    name = "Phone - Landscape (Light)",
    device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420",
    showSystemUi = true
)
@Preview(
    name = "Phone - Landscape (Dark)",
    device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Preview(
    name = "Tablet (Light)",
    device = TABLET,
    showSystemUi = true
)
@Preview(
    name = "Tablet (Dark)",
    device = TABLET,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Preview(
    name = "Desktop (Light)",
    device = DESKTOP,
    showSystemUi = true
)
@Preview(
    name = "Desktop (Dark)",
    device = DESKTOP,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
annotation class MultiDevicePreview

@MultiDevicePreview
@Composable
fun GamePadPreview() {
    PreviewBase {
        GamePad(null) {}
    }
}
