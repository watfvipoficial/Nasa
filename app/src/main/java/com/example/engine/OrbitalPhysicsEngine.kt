package com.example.engine

import kotlin.math.*
import kotlin.random.Random

data class CelestialBody(
    val id: String,
    val name: String,
    val position: Vector2D,
    val radius: Float,
    val mass: Float, // Arbitrary mass units for gravitational simulation
    val baseColor: Long,
    val atmosphereColor: Long,
    val hasRings: Boolean = false,
    val ringRadius: Float = 0f,
    val isBlackHole: Boolean = false
) {
    fun calculateGravityOn(targetPos: Vector2D, gravitationalConstant: Float = 14000f): Vector2D {
        val diff = position - targetPos
        val dist = diff.length()
        if (dist < 10f) return Vector2D.Zero
        // Inverse square law with softening parameter to prevent infinite velocity near center
        val softenedDist = max(dist, radius * 0.4f)
        val forceMagnitude = (gravitationalConstant * mass) / (softenedDist * softenedDist)
        return diff.normalized() * forceMagnitude
    }
}

data class OrbitalTelemetry(
    val altitude: Float,
    val orbitalSpeed: Float,
    val escapeSpeed: Float,
    val isStableOrbit: Boolean,
    val crashWarning: Boolean,
    val nearestBodyName: String
)

data class Particle(
    var position: Vector2D,
    var velocity: Vector2D,
    var color: Long,
    var alpha: Float = 1.0f,
    var size: Float,
    var life: Float, // 0.0 to 1.0
    var decayRate: Float,
    val isDebris: Boolean = false,
    var rotation: Float = 0f,
    var rotationSpeed: Float = 0f
)

data class Shockwave(
    val position: Vector2D,
    var radius: Float = 10f,
    val maxRadius: Float = 220f,
    var alpha: Float = 1.0f,
    val color: Long,
    var life: Float = 1.0f,
    val growthRate: Float = 360f
)

data class Projectile(
    val id: Long,
    var position: Vector2D,
    var velocity: Vector2D,
    val damage: Float,
    val color: Long,
    val isEnemy: Boolean,
    val isHoming: Boolean = false,
    val targetId: Long? = null,
    val radius: Float = 4f,
    var lifetime: Float = 2.5f,
    val isTorpedo: Boolean = false
)

data class SpaceEntity(
    val id: Long,
    var position: Vector2D,
    var velocity: Vector2D = Vector2D.Zero,
    var heading: Float = 0f, // in radians
    var targetHeading: Float = 0f,
    var hull: Float,
    val maxHull: Float,
    var shield: Float,
    val maxShield: Float,
    var energy: Float,
    val maxEnergy: Float,
    val shipName: String,
    val isBoss: Boolean = false,
    val isPlayer: Boolean = false,
    val radius: Float = 24f,
    val color: Long = 0xFF00E5FF,
    var attackCooldown: Float = 0f,
    var specialCooldown: Float = 0f,
    var shieldHitFlash: Float = 0f,
    var hullHitFlash: Float = 0f,
    var isDestroyed: Boolean = false,
    val scoreReward: Int = 100,
    val creditsReward: Int = 50,
    val exoticMatterReward: Int = 5
)

data class FloatingSalvage(
    val id: Long,
    var position: Vector2D,
    var velocity: Vector2D,
    val type: SalvageType,
    val value: Int,
    var lifetime: Float = 30f
)

enum class SalvageType {
    CREDITS_CONTAINER,
    EXOTIC_MATTER,
    HULL_REPAIR,
    SHIELD_BOOST
}

class OrbitalPhysicsEngine {
    val gravitationalConstant = 12500f

    fun calculateOrbitalTelemetry(
        shipPos: Vector2D,
        shipVel: Vector2D,
        bodies: List<CelestialBody>
    ): OrbitalTelemetry {
        val nearest = bodies.minByOrNull { it.position.distanceTo(shipPos) }
            ?: return OrbitalTelemetry(1000f, shipVel.length(), 500f, false, false, "Espacio Profundo")

        val dist = shipPos.distanceTo(nearest.position)
        val altitude = max(0f, dist - nearest.radius)
        val speed = shipVel.length()

        // Vis-viva circular orbital speed v_c = sqrt(G*M / r)
        val r = max(dist, nearest.radius)
        val circularSpeed = sqrt((gravitationalConstant * nearest.mass) / r)
        val escapeSpeed = circularSpeed * 1.4142f // sqrt(2) * v_c

        val speedDiff = abs(speed - circularSpeed)
        val isStable = speedDiff < (circularSpeed * 0.35f) && altitude > (nearest.radius * 0.3f)
        val crashWarning = altitude < (nearest.radius * 0.25f)

        return OrbitalTelemetry(
            altitude = altitude,
            orbitalSpeed = speed,
            escapeSpeed = escapeSpeed,
            isStableOrbit = isStable,
            crashWarning = crashWarning,
            nearestBodyName = nearest.name
        )
    }

    fun spawnExplosionParticles(
        pos: Vector2D,
        particles: MutableList<Particle>,
        shockwaves: MutableList<Shockwave>,
        isBoss: Boolean = false
    ) {
        val count = if (isBoss) 85 else 35
        val baseSpeed = if (isBoss) 380f else 220f
        val shockwaveRadius = if (isBoss) 320f else 180f

        shockwaves.add(
            Shockwave(
                position = pos,
                radius = 12f,
                maxRadius = shockwaveRadius,
                alpha = 1.0f,
                color = if (isBoss) 0xFFFF1744 else 0xFFFFB300,
                growthRate = if (isBoss) 420f else 320f
            )
        )

        val colors = if (isBoss) {
            listOf(0xFFFF1744, 0xFFFF6D00, 0xFFFFB300, 0xFFFFFFFF, 0xFFD500F9)
        } else {
            listOf(0xFFFFB300, 0xFFFF6D00, 0xFFFF3D00, 0xFF00E5FF, 0xFFFFFFFF)
        }

        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val spd = Random.nextFloat() * baseSpeed + 40f
            val vel = Vector2D.fromAngle(angle, spd)
            val isDebris = Random.nextFloat() > 0.65f
            particles.add(
                Particle(
                    position = pos + Vector2D.fromAngle(angle, Random.nextFloat() * 15f),
                    velocity = vel,
                    color = colors[Random.nextInt(colors.size)],
                    alpha = 1f,
                    size = if (isDebris) Random.nextFloat() * 6f + 3f else Random.nextFloat() * 4f + 2f,
                    life = 1f,
                    decayRate = Random.nextFloat() * 0.8f + 0.6f,
                    isDebris = isDebris,
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = (Random.nextFloat() - 0.5f) * 15f
                )
            )
        }
    }

    fun spawnThrusterParticles(
        shipPos: Vector2D,
        shipHeading: Float,
        shipVelocity: Vector2D,
        particles: MutableList<Particle>,
        count: Int = 3,
        color: Long = 0xFF00E5FF
    ) {
        val rearAngle = shipHeading + PI.toFloat()
        val engineOffset = Vector2D.fromAngle(rearAngle, 24f)
        val spawnPos = shipPos + engineOffset

        for (i in 0 until count) {
            val spreadAngle = rearAngle + (Random.nextFloat() - 0.5f) * 0.5f
            val speed = Random.nextFloat() * 140f + 160f
            val vel = Vector2D.fromAngle(spreadAngle, speed) + (shipVelocity * 0.3f)
            particles.add(
                Particle(
                    position = spawnPos + Vector2D(
                        (Random.nextFloat() - 0.5f) * 6f,
                        (Random.nextFloat() - 0.5f) * 6f
                    ),
                    velocity = vel,
                    color = if (Random.nextFloat() > 0.4f) color else 0xFFFFFFFF,
                    alpha = 0.9f,
                    size = Random.nextFloat() * 3.5f + 2f,
                    life = 1f,
                    decayRate = Random.nextFloat() * 3.0f + 2.5f
                )
            )
        }
    }
}
