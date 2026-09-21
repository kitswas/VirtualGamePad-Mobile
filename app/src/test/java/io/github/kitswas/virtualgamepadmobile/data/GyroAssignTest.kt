package io.github.kitswas.virtualgamepadmobile.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class GyroAssignTest {
    @Test
    fun levelOrientationIsCentered() {
        val (x, y) = gravityToStickDeflection(0f, 0f)
        assertEquals(0f, x, 0.001f)
        assertEquals(0f, y, 0.001f)
    }

    @Test
    fun slightTiltIsBeneathDeadzone() {
        val (x, y) = gravityToStickDeflection(0.3f, 0f)
        assertEquals(0f, x, 0.001f)
        assertEquals(0f, y, 0.001f)
    }

    @Test
    fun rightTiltProducesPositiveX() {
        val (x, y) = gravityToStickDeflection(6.0f, 0f)
        assertTrue(x > 0f)
        assertTrue(x <= 1f)
        assertEquals(0f, y, 0.001f)
    }

    @Test
    fun leftTiltProducesNegativeX() {
        val (x, y) = gravityToStickDeflection(-6.0f, 0f)
        assertTrue(x < 0f)
        assertTrue(x >= -1f)
        assertEquals(0f, y, 0.001f)
    }

    @Test
    fun topTiltProducesNegativeY() {
        val (x, y) = gravityToStickDeflection(0f, 6.0f)
        assertEquals(0f, x, 0.001f)
        assertTrue(y < 0f)
        assertTrue(y >= -1f)
    }

    @Test
    fun extremeTiltClampsToOne() {
        val (x, y) = gravityToStickDeflection(20f, 0f)
        assertTrue(x <= 1f)
        assertEquals(0f, y, 0.001f)
        val (x2, y2) = gravityToStickDeflection(0f, -20f)
        assertEquals(0f, x2, 0.001f)
        assertTrue(y2 <= 1f)
    }

    // Calibration reference behavior: deflection is relative to the captured reference,
    // so a steady hold at the reference pose is always centered, regardless of the
    // absolute gravity vector (fixes the "drifts right when holding a gamepad" issue).
    @Test
    fun matchingReferencePoseIsCentered() {
        val (x, y) = gravityToStickDeflection(7.0f, 4.5f, refX = 7.0f, refY = 4.5f)
        assertEquals(0f, x, 0.001f)
        assertEquals(0f, y, 0.001f)
    }

    @Test
    fun tiltAwayFromReferenceProducesDeflection() {
        val (x, y) = gravityToStickDeflection(9.0f, 4.5f, refX = 7.0f, refY = 4.5f)
        assertTrue(x > 0f)
        assertTrue(x <= 1f)
        assertEquals(0f, y, 0.001f)
    }

    @Test
    fun oppositeTiltFromReferenceProducesDeflection() {
        val (x, y) = gravityToStickDeflection(7.0f, -1.5f, refX = 7.0f, refY = 4.5f)
        assertEquals(0f, x, 0.001f)
        assertTrue(y > 0f)
        assertTrue(y <= 1f)
    }

    @Test
    fun matchingReferenceAzimuthIsCentered() {
        val steer = steerDeflectionFromAzimuth(azimuth = 1.2f, refAzimuth = 1.2f)
        assertEquals(0f, steer, 0.001f)
    }

    @Test
    fun slightYawIsBeneathDeadzone() {
        val steer = steerDeflectionFromAzimuth(azimuth = 0.02f, refAzimuth = 0f)
        assertEquals(0f, steer, 0.001f)
    }

    @Test
    fun clockwiseYawProducesPositiveSteer() {
        val steer = steerDeflectionFromAzimuth(azimuth = 0.4f, refAzimuth = 0f)
        assertTrue(steer > 0f)
        assertTrue(steer <= 1f)
    }

    @Test
    fun counterClockwiseYawProducesNegativeSteer() {
        val steer = steerDeflectionFromAzimuth(azimuth = -0.4f, refAzimuth = 0f)
        assertTrue(steer < 0f)
        assertTrue(steer >= -1f)
    }

    @Test
    fun wrapAroundPiDoesNotSpike() {
        // +3.10 rad and -3.10 rad are just either side of the ±π seam, so they are
        // geometrically close; naive subtraction would give -6.2 rad. The normalized
        // delta must stay small instead of spiking toward a full deflection.
        val steer = steerDeflectionFromAzimuth(azimuth = -3.10f, refAzimuth = 3.10f)
        assertTrue(steer > 0f)
        assertTrue(steer < 0.5f)
    }

    @Test
    fun extremeYawClampsToOne() {
        val steer = steerDeflectionFromAzimuth(azimuth = 2.5f, refAzimuth = 0f)
        assertTrue(steer <= 1f)
        val steer2 = steerDeflectionFromAzimuth(azimuth = -2.5f, refAzimuth = 0f)
        assertTrue(steer2 >= -1f)
    }

    @Test
    fun matchingReferenceBendIsCentered() {
        val steer = steerDeflectionFromRoll(roll = -0.5f, refRoll = -0.5f)
        assertEquals(0f, steer, 0.001f)
    }

    @Test
    fun topBendAwayFromReferenceProducesDeflection() {
        val steer = steerDeflectionFromRoll(roll = 0.35f, refRoll = 0f)
        assertTrue(steer > 0f)
        assertTrue(steer <= 1f)
    }

    @Test
    fun bottomBendFromReferenceProducesDeflection() {
        val steer = steerDeflectionFromRoll(roll = -0.35f, refRoll = 0f)
        assertTrue(steer < 0f)
        assertTrue(steer >= -1f)
    }

    @Test
    fun quarterTurnBendReachesFullDeflection() {
        // The full range is a quarter turn: with the phone's back fully toward the
        // ceiling/ground the bend is ±π/2, which must drive the stick to its maximum.
        val up = steerDeflectionFromRoll(roll = (PI / 2).toFloat(), refRoll = 0f)
        assertEquals(1f, up, 0.001f)
        val down = steerDeflectionFromRoll(roll = (-PI / 2).toFloat(), refRoll = 0f)
        assertEquals(-1f, down, 0.001f)
    }

    @Test
    fun restingHoldAtTheReferenceIsCentered() {
        // The reference is captured at the rest (gamepad) pose, so even a fairly bent
        // hold that matches the captured reference must produce a centered stick.
        val atRef = steerDeflectionFromRoll(roll = 0.8f, refRoll = 0.8f)
        assertEquals(0f, atRef, 0.001f)
    }

    @Test
    fun fromConfigsDerivesAggregateAssignment() {
        val configs = mapOf(
            ButtonComponent.LEFT_ANALOG_STICK to ButtonConfig(ButtonComponent.LEFT_ANALOG_STICK),
            ButtonComponent.RIGHT_ANALOG_STICK to ButtonConfig(ButtonComponent.RIGHT_ANALOG_STICK),
            ButtonComponent.DPAD to ButtonConfig(ButtonComponent.DPAD)
        )

        assertEquals(GyroAssign.OFF, GyroAssign.fromConfigs(configs))

        val leftOnly = configs.toMutableMap()
        leftOnly[ButtonComponent.LEFT_ANALOG_STICK] =
            leftOnly[ButtonComponent.LEFT_ANALOG_STICK]!!.copy(gyro = true)
        assertEquals(GyroAssign.LEFT, GyroAssign.fromConfigs(leftOnly))

        val both = leftOnly.toMutableMap()
        both[ButtonComponent.RIGHT_ANALOG_STICK] =
            both[ButtonComponent.RIGHT_ANALOG_STICK]!!.copy(gyro = true)
        assertEquals(GyroAssign.BOTH, GyroAssign.fromConfigs(both))
    }

    @Test
    fun applyToSetsTheRightStickFlags() {
        val configs = mapOf(
            ButtonComponent.LEFT_ANALOG_STICK to ButtonConfig(ButtonComponent.LEFT_ANALOG_STICK),
            ButtonComponent.RIGHT_ANALOG_STICK to ButtonConfig(ButtonComponent.RIGHT_ANALOG_STICK),
            ButtonComponent.DPAD to ButtonConfig(ButtonComponent.DPAD)
        )

        val right = GyroAssign.RIGHT.applyTo(configs)
        assertFalse(right[ButtonComponent.LEFT_ANALOG_STICK]!!.gyro)
        assertTrue(right[ButtonComponent.RIGHT_ANALOG_STICK]!!.gyro)

        val both = GyroAssign.BOTH.applyTo(configs)
        assertTrue(both[ButtonComponent.LEFT_ANALOG_STICK]!!.gyro)
        assertTrue(both[ButtonComponent.RIGHT_ANALOG_STICK]!!.gyro)
    }
}