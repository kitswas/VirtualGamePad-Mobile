package io.github.kitswas.virtualgamepadmobile.ui.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.data.MotionStickControl
import kotlin.math.PI

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

@Composable
fun BindMotionStickControl(
    gamepadState: GamepadReading,
    motionStickControl: MotionStickControl,
    motionSensitivity: Float,
    motionStickX: MutableState<Float>,
    motionStickY: MutableState<Float>,
) {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val motionSensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }
    val displayRotation = context.findActivity()?.display?.rotation ?: Surface.ROTATION_0

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
}
