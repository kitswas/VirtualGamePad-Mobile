package io.github.kitswas.virtualgamepadmobile.data

import android.hardware.SensorManager
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Which analog stick(s) (if any) are driven by the device's tilt sensors.
 * Mirrors the per-stick `gyro` flags stored in each [ButtonConfig].
 */
enum class GyroAssign(val displayName: String) {
    OFF("Off"),
    LEFT("Left Stick"),
    RIGHT("Right Stick"),
    BOTH("Both Sticks");

    /**
     * Derives the aggregate assignment from the per-stick flags in a config map.
     */
    companion object {
        fun fromConfigs(configs: Map<ButtonComponent, ButtonConfig>): GyroAssign {
            val left = configs[ButtonComponent.LEFT_ANALOG_STICK]?.gyro == true
            val right = configs[ButtonComponent.RIGHT_ANALOG_STICK]?.gyro == true
            return when {
                left && right -> BOTH
                left -> LEFT
                right -> RIGHT
                else -> OFF
            }
        }
    }

    /**
     * Applies this assignment to a copy of the given config map, updating each
     * analog stick's `gyro` flag.
     */
    fun applyTo(configs: Map<ButtonComponent, ButtonConfig>): Map<ButtonComponent, ButtonConfig> {
        val updated = configs.toMutableMap()
        configs[ButtonComponent.LEFT_ANALOG_STICK]?.let { current ->
            updated[ButtonComponent.LEFT_ANALOG_STICK] = current.copy(gyro = this == LEFT || this == BOTH)
        }
        configs[ButtonComponent.RIGHT_ANALOG_STICK]?.let { current ->
            updated[ButtonComponent.RIGHT_ANALOG_STICK] = current.copy(gyro = this == RIGHT || this == BOTH)
        }
        return updated
    }
}

private val DEFAULT_GYRO_DEADZONE = 0.06f
private const val GYRO_FULL_TILT = 1.1f
private const val GRAVITY_EARTH = SensorManager.GRAVITY_EARTH

private const val DEFAULT_YAW_DEADZONE = 0.05f
// Horizontal (yaw/azimuth) rotation, in radians, that corresponds to a full stick
// deflection (like turning a steering wheel).
private const val GYRO_FULL_YAW = 0.6f
// The same gesture, mapped to the vertical axis: how far from "back fully toward the
// ceiling" to "back fully toward the ground", in radians, produces a full deflection.
// Mirroring [steerDeflectionFromAzimuth] keeps both axes consistent: X responds to
// horizontal heading (azimuth o[0], confirmed working on-device) and Y responds to the
// phone's own roll (o[2], the short-edge bend toward ceiling/ground) through the same
// deadzone + deadzone-scaling pipeline.
private const val GYRO_FULL_ROLL = 0.6f
private val PI_RAD = PI.toFloat()

/**
 * Maps the device gravity vector (in m/s² from a TYPE_GRAVITY/SIGNIFICANT_MOTION sensor)
 * to normalized stick deflection in the range -1..1.
 *
 * The gravity reading captured when the user holds the phone in the neutral "gamepad
 * position" is passed as the calibration reference ([refX], [refY]). Deflection is
 * measured relative to that reference, so a steady hold produces a centered stick
 * regardless of the absolute tilt of the phone.
 *
 * @param gx side-to-side tilt (positive = right edge down)
 * @param gy forward/back tilt (negative = top edge down)
 * @param refX calibrated gravity x-component (at neutral hold)
 * @param refY calibrated gravity y-component (at neutral hold)
 */
internal fun gravityToStickDeflection(
    gx: Float,
    gy: Float,
    refX: Float = 0f,
    refY: Float = 0f,
): Pair<Float, Float> {
    val rawX = ((gx - refX) / GRAVITY_EARTH).coerceIn(-1f, 1f) / GYRO_FULL_TILT
    val rawY = (-(gy - refY) / GRAVITY_EARTH).coerceIn(-1f, 1f) / GYRO_FULL_TILT

    val magnitude = sqrt(rawX * rawX + rawY * rawY)
    if (magnitude <= DEFAULT_GYRO_DEADZONE) return 0f to 0f

    // Remap the dead-zoned magnitude so full tilt still reaches 1.0
    val scaled = (magnitude - DEFAULT_GYRO_DEADZONE) / (1f - DEFAULT_GYRO_DEADZONE)
    val factor = scaled / magnitude
    return rawX * factor to rawY * factor
}

/**
 * Maps horizontal rotation (yaw, in radians) of the phone to normalized left/right
 * deflection in the range -1..1.
 *
 * The yaw is rotation around the world-vertical axis: holding a phone in a gamepad grip
 * and turning it like a steering wheel pushes one side away from the user (and the other
 * toward them). Unlike the gravity vector, this cannot be measured from a tilt/gravity
 * sensor (gravity is unchanged by yaw), so it is derived from the rotation-vector
 * sensor's azimuth.
 *
 * @param azimuth current heading angle in radians (rotation about the world -z axis)
 * @param refAzimuth calibrated heading at the neutral hold
 * @param fullSteer radians of horizontal rotation that produce a full deflection
 * @param deadzone radians of rotation left untouched around center
 */
internal fun steerDeflectionFromAzimuth(
    azimuth: Float,
    refAzimuth: Float,
    fullSteer: Float = GYRO_FULL_YAW,
    deadzone: Float = DEFAULT_YAW_DEADZONE,
): Float {
    val delta = normalizeRotationDelta(azimuth - refAzimuth)
    return deflectionFromRotationDelta(delta, fullSteer, deadzone)
}

/**
 * Maps the top/bottom edge roll (in radians) of the phone to normalized up/down
 * deflection in the range -1..1. Mirrors [steerDeflectionFromAzimuth] for the short
 * edges of the phone.
 *
 * In a landscape gamepad grip the long edges are held by the hands, so bending the
 * top/bottom edge toward the ceiling/ground is a rotation about the long axis. That
 * rotation is the device's roll: [SensorManager.getOrientation] reports it in
 * element [2] of the orientation array (roll = rotation about the device's forward
 * axis, measured as the vertical lift of the top/bottom edge).
 *
 * @param roll current top/bottom edge roll angle in radians (positive = top edge up)
 * @param refRoll calibrated angle at the neutral hold
 * @param fullRoll radians of roll that produce a full deflection
 * @param deadzone radians of rotation left untouched around center
 */
internal fun steerDeflectionFromRoll(
    roll: Float,
    refRoll: Float,
    fullRoll: Float = GYRO_FULL_ROLL,
    deadzone: Float = DEFAULT_YAW_DEADZONE,
): Float {
    val delta = normalizeRotationDelta(roll - refRoll)
    return deflectionFromRotationDelta(delta, fullRoll, deadzone)
}

private fun normalizeRotationDelta(delta: Float): Float {
    var d = delta
    while (d > PI_RAD) d -= 2f * PI_RAD
    while (d < -PI_RAD) d += 2f * PI_RAD
    return d
}

private fun deflectionFromRotationDelta(delta: Float, full: Float, deadzone: Float): Float {
    val raw = (delta / full).coerceIn(-1f, 1f)
    if (abs(raw) <= deadzone) return 0f

    // Remap the dead-zoned magnitude so a full rotation still reaches 1.0
    val scaled = (abs(raw) - deadzone) / (1f - deadzone)
    return if (raw < 0f) -scaled else scaled
}