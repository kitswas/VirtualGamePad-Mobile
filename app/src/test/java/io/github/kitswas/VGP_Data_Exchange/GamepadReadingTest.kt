package io.github.kitswas.VGP_Data_Exchange

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class GamepadReadingTest {

    private lateinit var gamepadReading: GamepadReading
    val floatDelta = 0.001f

    @Before
    fun setUp() {
        gamepadReading = GamepadReading()
    }

    @Test
    fun `test default constructor initializes with zero values`() {
        assertEquals(0, gamepadReading.ButtonsUp)
        assertEquals(0, gamepadReading.ButtonsDown)
        assertEquals(0f, gamepadReading.LeftTrigger, floatDelta)
        assertEquals(0f, gamepadReading.RightTrigger, floatDelta)
        assertEquals(0f, gamepadReading.LeftThumbstickX, floatDelta)
        assertEquals(0f, gamepadReading.LeftThumbstickY, floatDelta)
        assertEquals(0f, gamepadReading.RightThumbstickX, floatDelta)
        assertEquals(0f, gamepadReading.RightThumbstickY, floatDelta)
    }

    @Test
    fun `test getters and setters`() {
        // Set values using setters
        gamepadReading.buttonsUp = GameButtons.A.value
        gamepadReading.buttonsDown = GameButtons.B.value
        gamepadReading.leftTrigger = 0.5f
        gamepadReading.rightTrigger = 0.75f
        gamepadReading.leftThumbstickX = -0.25f
        gamepadReading.leftThumbstickY = 0.25f
        gamepadReading.rightThumbstickX = 0.8f
        gamepadReading.rightThumbstickY = -0.8f

        // Verify using getters
        assertEquals(GameButtons.A.value, gamepadReading.buttonsUp)
        assertEquals(GameButtons.B.value, gamepadReading.buttonsDown)
        assertEquals(0.5f, gamepadReading.leftTrigger, floatDelta)
        assertEquals(0.75f, gamepadReading.rightTrigger, floatDelta)
        assertEquals(-0.25f, gamepadReading.leftThumbstickX, floatDelta)
        assertEquals(0.25f, gamepadReading.leftThumbstickY, floatDelta)
        assertEquals(0.8f, gamepadReading.rightThumbstickX, floatDelta)
        assertEquals(-0.8f, gamepadReading.rightThumbstickY, floatDelta)
    }

    @Test
    fun `test fluent API with chaining`() {
        gamepadReading
            .withButtonsUp(GameButtons.X.value)
            .withButtonsDown(GameButtons.Y.value)
            .withLeftTrigger(0.33f)
            .withRightTrigger(0.66f)
            .withLeftThumbstickX(0.1f)
            .withLeftThumbstickY(0.2f)
            .withRightThumbstickX(0.3f)
            .withRightThumbstickY(0.4f)

        assertEquals(GameButtons.X.value, gamepadReading.ButtonsUp)
        assertEquals(GameButtons.Y.value, gamepadReading.ButtonsDown)
        assertEquals(0.33f, gamepadReading.LeftTrigger, floatDelta)
        assertEquals(0.66f, gamepadReading.RightTrigger, floatDelta)
        assertEquals(0.1f, gamepadReading.LeftThumbstickX, floatDelta)
        assertEquals(0.2f, gamepadReading.LeftThumbstickY, floatDelta)
        assertEquals(0.3f, gamepadReading.RightThumbstickX, floatDelta)
        assertEquals(0.4f, gamepadReading.RightThumbstickY, floatDelta)
    }

    @Test
    fun `test marshal and unmarshal with multiple button combinations`() {
        // Test with various button combinations
        val testCases = listOf(
            Triple(GameButtons.A.value, GameButtons.None.value, "A pressed"),
            Triple(
                GameButtons.B.value or GameButtons.X.value,
                GameButtons.None.value,
                "B and X pressed"
            ),
            Triple(
                GameButtons.None.value,
                GameButtons.A.value or GameButtons.B.value,
                "A and B released"
            ),
            Triple(
                GameButtons.LeftShoulder.value or GameButtons.RightShoulder.value,
                GameButtons.LeftThumbstick.value,
                "Shoulder buttons pressed and left thumbstick released"
            )
        )

        for ((buttonsUp, buttonsDown, description) in testCases) {
            val original = GamepadReading().apply {
                this.ButtonsUp = buttonsUp
                this.ButtonsDown = buttonsDown
                this.LeftTrigger = 0.5f
                this.RightTrigger = 0.75f
                this.LeftThumbstickX = -0.5f
                this.LeftThumbstickY = 0.5f
                this.RightThumbstickX = 0.25f
                this.RightThumbstickY = -0.25f
            }

            val bytes = ByteArrayOutputStream().use { stream ->
                original.marshal(stream, null)
                stream.toByteArray()
            }

            // Unmarshal and verify
            val unmarshalled = GamepadReading()
            unmarshalled.unmarshal(bytes, 0)

            assertEquals(
                "ButtonsUp should match for $description",
                original.ButtonsUp,
                unmarshalled.ButtonsUp
            )
            assertEquals(
                "ButtonsDown should match for $description",
                original.ButtonsDown,
                unmarshalled.ButtonsDown
            )
            assertEquals(
                "LeftTrigger should match for $description",
                original.LeftTrigger,
                unmarshalled.LeftTrigger,
                floatDelta
            )
            assertEquals(
                "RightTrigger should match for $description",
                original.RightTrigger,
                unmarshalled.RightTrigger,
                floatDelta
            )
            assertEquals(
                "LeftThumbstickX should match for $description",
                original.LeftThumbstickX,
                unmarshalled.LeftThumbstickX,
                floatDelta
            )
            assertEquals(
                "LeftThumbstickY should match for $description",
                original.LeftThumbstickY,
                unmarshalled.LeftThumbstickY,
                floatDelta
            )
            assertEquals(
                "RightThumbstickX should match for $description",
                original.RightThumbstickX,
                unmarshalled.RightThumbstickX,
                floatDelta
            )
            assertEquals(
                "RightThumbstickY should match for $description",
                original.RightThumbstickY,
                unmarshalled.RightThumbstickY,
                floatDelta
            )

            assertTrue("Objects should be equal for $description", original == unmarshalled)
            assertEquals(
                "HashCodes should match for $description",
                original.hashCode(),
                unmarshalled.hashCode()
            )
        }
    }

    @Test
    fun `test unmarshal with Unmarshaller class`() {
        val original = GamepadReading().apply {
            this.ButtonsUp = GameButtons.A.value or GameButtons.B.value
            this.ButtonsDown = GameButtons.X.value
            this.LeftTrigger = 0.9f
            this.RightTrigger = 0.8f
            this.LeftThumbstickX = 0.7f
            this.LeftThumbstickY = 0.6f
            this.RightThumbstickX = 0.5f
            this.RightThumbstickY = 0.4f
        }

        // Marshal the object to a byte array
        val bytes = ByteArrayOutputStream().use { stream ->
            original.marshal(stream, null)
            stream.toByteArray()
        }

        // Create a piped input stream from the bytes
        val inputStream = ByteArrayInputStream(bytes)

        // Use the Unmarshaller to deserialize
        val unmarshaller = GamepadReading.Unmarshaller(inputStream, ByteArray(2048))
        val result = unmarshaller.next()

        // Verify the result
        assertNotNull("Unmarshalled result should not be null", result)
        assertEquals("ButtonsUp should match", original.ButtonsUp, result.ButtonsUp)
        assertEquals("ButtonsDown should match", original.ButtonsDown, result.ButtonsDown)
        assertEquals(
            "LeftTrigger should match",
            original.LeftTrigger,
            result.LeftTrigger,
            floatDelta
        )
        assertEquals(
            "RightTrigger should match",
            original.RightTrigger,
            result.RightTrigger,
            floatDelta
        )
        assertEquals(
            "LeftThumbstickX should match",
            original.LeftThumbstickX,
            result.LeftThumbstickX,
            floatDelta
        )
        assertEquals(
            "LeftThumbstickY should match",
            original.LeftThumbstickY,
            result.LeftThumbstickY,
            floatDelta
        )
        assertEquals(
            "RightThumbstickX should match",
            original.RightThumbstickX,
            result.RightThumbstickX,
            floatDelta
        )
        assertEquals(
            "RightThumbstickY should match",
            original.RightThumbstickY,
            result.RightThumbstickY,
            floatDelta
        )
    }

    @Test
    fun `test equals and hashCode`() {
        val reading1 = GamepadReading().apply {
            ButtonsUp = GameButtons.A.value
            ButtonsDown = GameButtons.B.value
            LeftTrigger = 0.5f
            RightTrigger = 0.6f
            LeftThumbstickX = 0.1f
            LeftThumbstickY = 0.2f
            RightThumbstickX = 0.3f
            RightThumbstickY = 0.4f
        }

        val reading2 = GamepadReading().apply {
            ButtonsUp = GameButtons.A.value
            ButtonsDown = GameButtons.B.value
            LeftTrigger = 0.5f
            RightTrigger = 0.6f
            LeftThumbstickX = 0.1f
            LeftThumbstickY = 0.2f
            RightThumbstickX = 0.3f
            RightThumbstickY = 0.4f
        }

        val reading3 = GamepadReading().apply {
            ButtonsUp = GameButtons.X.value // Different value
            ButtonsDown = GameButtons.B.value
            LeftTrigger = 0.5f
            RightTrigger = 0.6f
            LeftThumbstickX = 0.1f
            LeftThumbstickY = 0.2f
            RightThumbstickX = 0.3f
            RightThumbstickY = 0.4f
        }

        // Test equality for same values
        assertEquals(reading1, reading2)
        assertEquals(reading1.hashCode(), reading2.hashCode())

        // Test inequality for different values
        assertNotEquals(reading1, reading3)
        assertNotEquals(reading1.hashCode(), reading3.hashCode())

        // Test equality with self
        assertEquals(reading1, reading1)

        // Test inequality with null
        assertNotEquals(reading1, null)

        // Test inequality with different class
        assertNotEquals(reading1, "Not a GamepadReading")
    }

    @Test
    fun `test marshalFit size estimate`() {
        // MarshalFit should return a reasonable size estimate
        val size = gamepadReading.marshalFit()
        assertTrue("Size estimate should be positive", size > 0)
        assertTrue("Size estimate should be within max size", size <= GamepadReading.colferSizeMax)
    }
}
