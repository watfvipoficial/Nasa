package com.example

import com.example.engine.CelestialBody
import com.example.engine.OrbitalPhysicsEngine
import com.example.engine.Vector2D
import com.example.model.ShipClass
import com.example.model.ShipCustomization
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testVector2DMath() {
        val v1 = Vector2D(3f, 4f)
        assertEquals(5f, v1.length(), 0.001f)

        val v2 = Vector2D(1f, 2f)
        val sum = v1 + v2
        assertEquals(4f, sum.x, 0.001f)
        assertEquals(6f, sum.y, 0.001f)

        val norm = v1.normalized()
        assertEquals(1f, norm.length(), 0.001f)
    }

    @Test
    fun testCelestialGravityCalculation() {
        val earth = CelestialBody(
            id = "earth",
            name = "La Tierra",
            position = Vector2D.Zero,
            radius = 200f,
            mass = 100f,
            baseColor = 0xFF1976D2,
            atmosphereColor = 0xFF80D8FF
        )

        val shipPos = Vector2D(0f, 400f)
        val gravity = earth.calculateGravityOn(shipPos)

        // Gravity pulls towards earth center (negative y direction)
        assertTrue(gravity.y < 0f)
        assertEquals(0f, gravity.x, 0.001f)
    }

    @Test
    fun testShipCustomizationCalculations() {
        val scout = ShipCustomization(shipClass = ShipClass.ARTEMIS_SCOUT)
        assertEquals(550f, scout.totalMaxHull, 0.001f)
        assertEquals(400f, scout.totalMaxShield, 0.001f)
        assertTrue(scout.totalSpeed > 0f)
    }

    @Test
    fun testOrbitalTelemetry() {
        val engine = OrbitalPhysicsEngine()
        val earth = CelestialBody(
            id = "earth",
            name = "La Tierra",
            position = Vector2D.Zero,
            radius = 200f,
            mass = 100f,
            baseColor = 0xFF1976D2,
            atmosphereColor = 0xFF80D8FF
        )

        val telemetry = engine.calculateOrbitalTelemetry(
            shipPos = Vector2D(0f, 500f),
            shipVel = Vector2D(300f, 0f),
            bodies = listOf(earth)
        )

        assertEquals("La Tierra", telemetry.nearestBodyName)
        assertEquals(300f, telemetry.altitude, 0.001f)
        assertTrue(telemetry.orbitalSpeed > 0f)
    }

    @Test
    fun testCombatLogging() {
        val event = com.example.model.CombatLogEvent(
            id = 1L,
            timestampSeconds = 45f,
            timeLabel = "T+00:45",
            message = "Test tactical event",
            type = com.example.model.CombatEventType.TACTICAL_INFO
        )

        assertEquals("T+00:45", event.timeLabel)
        assertEquals("Test tactical event", event.message)
        assertEquals(com.example.model.CombatEventType.TACTICAL_INFO, event.type)
        assertEquals("INFO", event.type.tag)
    }

    @Test
    fun testHapticPatterns() {
        val patterns = com.example.haptics.HapticPattern.entries
        assertTrue(patterns.contains(com.example.haptics.HapticPattern.KINETIC_IMPACT))
        assertTrue(patterns.contains(com.example.haptics.HapticPattern.EXPLOSION_HEAVY))
        assertTrue(patterns.contains(com.example.haptics.HapticPattern.SHIELD_COLLAPSE))
        assertTrue(patterns.contains(com.example.haptics.HapticPattern.ORDNANCE_LAUNCH))
    }
}
