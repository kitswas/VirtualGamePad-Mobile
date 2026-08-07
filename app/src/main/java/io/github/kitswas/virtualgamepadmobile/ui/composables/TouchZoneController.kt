package io.github.kitswas.virtualgamepadmobile.ui.composables

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Maximum number of simultaneous fingers that the button layer (D-pad, face
 * buttons, shoulder buttons, menu buttons) will track independently.
 *
 * Previously every button drove its own [androidx.compose.foundation.clickable]
 * gesture detector, which meant presses were only ever resolved one pointer at
 * a time per button and there was no coordination between buttons at all. Now
 * that a single [MultiTouchController] resolves every pointer against every
 * button's on-screen bounds, this constant is the one place that controls how
 * many fingers can be doing something on the button cluster at once. 4 covers
 * realistic play (e.g. two fingers rolling across the face buttons while the
 * other hand holds a shoulder button and a D-pad direction), with room to
 * raise it later if needed.
 */
const val MAX_TRACKED_TOUCH_POINTS = 4

/**
 * A rectangular hit-region on screen that represents one gamepad button.
 */
private class TouchZone(
    val id: String,
    var bounds: Rect,
    var onPress: () -> Unit,
    var onRelease: () -> Unit,
)

/**
 * Tracks which on-screen zones (buttons) are activated by which fingers, and
 * drives the "rollover" (a.k.a. "slide-to-switch") gesture: if a finger that
 * is already down drags out of the button it originally pressed and into a
 * neighbouring button - without ever lifting off the glass - the original
 * button is released and the new one is pressed automatically. This is the
 * same interaction real button matrices (and things like on-screen piano/
 * combo-fighter pads) use, and it lets a player "roll" from one face button
 * to another with a single continuous touch instead of having to lift and
 * re-tap.
 *
 * A single controller instance is shared by every button that should
 * participate in rollover together (see [LocalMultiTouchController]); each
 * button registers its bounds and press/release callbacks with
 * [registerZone], and one [Modifier.multiTouchDispatcher] on the shared
 * container does the actual per-pointer hit-testing.
 */
class MultiTouchController {

    private val zones = mutableMapOf<String, TouchZone>()

    /** Live "is this zone currently pressed" state, keyed by zone id. */
    val pressedZones: SnapshotStateMap<String, Boolean> = mutableStateMapOf()

    // Which zone (if any) each currently-down pointer is activating.
    private val pointerToZone = mutableMapOf<PointerId, String>()

    fun registerZone(id: String, bounds: Rect, onPress: () -> Unit, onRelease: () -> Unit) {
        val existing = zones[id]
        if (existing != null) {
            existing.bounds = bounds
            existing.onPress = onPress
            existing.onRelease = onRelease
        } else {
            zones[id] = TouchZone(id, bounds, onPress, onRelease)
        }
        if (id !in pressedZones) pressedZones[id] = false
    }

    fun updateBounds(id: String, bounds: Rect) {
        zones[id]?.bounds = bounds
    }

    fun unregisterZone(id: String) {
        // Don't leave a button stuck in the "pressed" state if it's removed
        // (e.g. hidden by customization settings) while a finger is on it.
        releaseZoneIfPressed(id)
        zones.remove(id)
        pressedZones.remove(id)
        pointerToZone.entries.removeAll { it.value == id }
    }

    private fun releaseZoneIfPressed(id: String) {
        if (pressedZones[id] == true) {
            pressedZones[id] = false
            zones[id]?.onRelease?.invoke()
        }
    }

    private fun hitTest(position: Offset): TouchZone? =
        zones.values.firstOrNull { it.bounds.contains(position) }

    /**
     * Called for every pointer that is currently down, with its latest
     * position (in the same coordinate space the zone bounds were
     * registered in). Presses the zone under the finger, releasing whatever
     * zone that same finger was previously pressing if it has moved to a
     * different one (or off every button).
     */
    fun onPointerMoved(pointerId: PointerId, position: Offset) {
        val newZone = hitTest(position)
        val previousZoneId = pointerToZone[pointerId]

        if (newZone?.id == previousZoneId) return // still on the same button (or still off every button)

        previousZoneId?.let { releaseZoneIfPressed(it) }

        if (newZone != null) {
            pointerToZone[pointerId] = newZone.id
            pressedZones[newZone.id] = true
            newZone.onPress()
        } else {
            pointerToZone.remove(pointerId)
        }
    }

    /** Called when a pointer is lifted or the gesture is cancelled. */
    fun onPointerReleased(pointerId: PointerId) {
        val zoneId = pointerToZone.remove(pointerId) ?: return
        releaseZoneIfPressed(zoneId)
    }

    /** Releases everything immediately, e.g. when the gamepad screen is torn down. */
    fun releaseAll() {
        pointerToZone.clear()
        zones.keys.toList().forEach { releaseZoneIfPressed(it) }
    }
}

/**
 * Provides the [MultiTouchController] shared by every button in the current
 * button cluster. `null` (the default) means "no shared controller" - button
 * composables fall back to being non-interactive in that case, which happens
 * in isolated `@Preview`s that don't set up a container.
 */
val LocalMultiTouchController = compositionLocalOf<MultiTouchController?> { null }

/**
 * Provides the [LayoutCoordinates] of the container that hosts the button
 * cluster. Button composables translate their own position into this
 * container's local space (via [LayoutCoordinates.localBoundingBoxOf]) so
 * that zone bounds and pointer positions from [Modifier.multiTouchDispatcher]
 * (which reports positions local to the same container) line up exactly,
 * regardless of where in the layout hierarchy a given button lives.
 */
val LocalTouchContainerCoordinates = compositionLocalOf<LayoutCoordinates?> { null }

/**
 * Attaches to the container that hosts an entire button cluster (see
 * [DrawGamepad]). Performs low-level, manual multi-pointer tracking - up to
 * [MAX_TRACKED_TOUCH_POINTS] pointers at once - and forwards every pointer's
 * position to [controller] every time it moves, and its removal when it is
 * lifted. This is what actually implements both requested behaviours:
 *  - multiple fingers pressing different buttons simultaneously, and
 *  - "rollover": dragging one already-down finger from one button to another.
 */
fun Modifier.multiTouchDispatcher(controller: MultiTouchController): Modifier =
    this.pointerInput(controller) {
        val activePointers = mutableSetOf<PointerId>()
        awaitEachGesture {
            do {
                val event = awaitPointerEvent()
                for (change in event.changes) {
                    if (change.pressed) {
                        val isNewPointer = change.id !in activePointers
                        if (isNewPointer && activePointers.size >= MAX_TRACKED_TOUCH_POINTS) {
                            // Already tracking the maximum number of fingers on the
                            // button layer; ignore any further simultaneous touches
                            // rather than silently stealing a slot from one already
                            // in use.
                            continue
                        }
                        if (isNewPointer) activePointers.add(change.id)
                        controller.onPointerMoved(change.id, change.position)
                        if (change.positionChanged() || isNewPointer) {
                            change.consume()
                        }
                    } else if (change.id in activePointers) {
                        activePointers.remove(change.id)
                        controller.onPointerReleased(change.id)
                        change.consume()
                    }
                }
            } while (event.changes.any { it.pressed })
            // Safety net: make sure nothing is left "stuck" pressed if the
            // gesture ends in a way that doesn't cleanly report every pointer up
            // (e.g. the system cancels the gesture).
            activePointers.forEach { controller.onPointerReleased(it) }
            activePointers.clear()
        }
    }

/**
 * Registers the composable this modifier is attached to as a named touch
 * zone (button) on [LocalMultiTouchController], keeping its bounds in sync
 * as it moves/resizes, and unregistering it when it leaves composition.
 *
 * [onPress] and [onRelease] should perform whatever side effects the button
 * used to perform from `DisposableEffect(isPressed) { ... }` - haptics,
 * flipping bits in [io.github.kitswas.VGP_Data_Exchange.GamepadReading], etc.
 * They're wrapped with [rememberUpdatedState] internally, so callers don't
 * need to worry about stale captures across recompositions.
 *
 * If no [MultiTouchController] is available (e.g. an isolated `@Preview`),
 * this is a no-op and the button simply won't be interactive - existing
 * previews that don't care about touch behaviour are unaffected.
 */
@Composable
fun Modifier.touchZone(
    id: String,
    onPress: () -> Unit,
    onRelease: () -> Unit,
): Modifier {
    val controller = LocalMultiTouchController.current
    val containerCoordinates = LocalTouchContainerCoordinates.current
    val currentOnPress by rememberUpdatedState(onPress)
    val currentOnRelease by rememberUpdatedState(onRelease)

    DisposableEffect(controller, id) {
        onDispose { controller?.unregisterZone(id) }
    }

    if (controller == null) return this

    return this.onGloballyPositioned { coordinates ->
        val container = containerCoordinates
        if (container == null || !coordinates.isAttached || !container.isAttached) return@onGloballyPositioned
        val boundsInContainer = container.localBoundingBoxOf(coordinates)
        controller.registerZone(
            id = id,
            bounds = boundsInContainer,
            onPress = { currentOnPress() },
            onRelease = { currentOnRelease() },
        )
    }
}
