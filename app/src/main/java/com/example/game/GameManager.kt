package com.example.game

import com.example.audio.AudioEngine
import com.example.audio.SfxType
import com.example.campaign.CampaignData
import com.example.engine.*
import com.example.haptics.HapticPattern
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*
import kotlin.random.Random

data class GameUIState(
    val playerHull: Float = 1000f,
    val playerMaxHull: Float = 1000f,
    val playerShield: Float = 800f,
    val playerMaxShield: Float = 800f,
    val playerEnergy: Float = 150f,
    val playerMaxEnergy: Float = 150f,
    val speedKmh: Float = 0f,
    val altitudeKm: Float = 0f,
    val nearestBody: String = "Tierra",
    val isStableOrbit: Boolean = true,
    val crashWarning: Boolean = false,
    val lockedTargetName: String? = null,
    val lockedTargetDistance: Float = 0f,
    val lockedTargetHullPercent: Float = 0f,
    val enemiesRemaining: Int = 0,
    val bossActive: Boolean = false,
    val bossName: String = "",
    val bossHealthPercent: Float = 1f,
    val currentScore: Int = 0,
    val creditsEarned: Int = 0,
    val exoticMatterEarned: Int = 0,
    val isMissionAccomplished: Boolean = false,
    val isGameOver: Boolean = false,
    val powerSystem: PowerSystem = PowerSystem.BALANCED,
    val cameraShake: Float = 0f,
    val screenFlashColor: Long? = null,
    val secondaryCooldownPercent: Float = 0f,
    val combatLog: List<CombatLogEvent> = emptyList()
)

class GameManager(
    val audioEngine: AudioEngine,
    val onVibratePattern: ((HapticPattern) -> Unit)? = null
) {
    private val _uiState = MutableStateFlow(GameUIState())
    val uiState: StateFlow<GameUIState> = _uiState.asStateFlow()

    val physicsEngine = OrbitalPhysicsEngine()

    var playerCustomization = ShipCustomization()
    var currentSector: CampaignSector = CampaignData.initialSectors[0]

    // Celestial bodies in space arena
    val celestialBodies = mutableListOf<CelestialBody>()

    // Space entities
    var playerShip = SpaceEntity(
        id = 1L,
        position = Vector2D(0f, -420f),
        velocity = Vector2D(310f, 0f), // Initial orbital velocity
        heading = 0f,
        hull = 1000f,
        maxHull = 1000f,
        shield = 800f,
        maxShield = 800f,
        energy = 150f,
        maxEnergy = 150f,
        shipName = "NASA Vanguard",
        isPlayer = true,
        radius = 28f
    )

    val enemies = mutableListOf<SpaceEntity>()
    val projectiles = mutableListOf<Projectile>()
    val particles = mutableListOf<Particle>()
    val shockwaves = mutableListOf<Shockwave>()
    val salvages = mutableListOf<FloatingSalvage>()

    var lockedTargetId: Long? = null
    var powerSystem: PowerSystem = PowerSystem.BALANCED
    var flightAssistEnabled: Boolean = true

    // Controls input
    var thrustVector: Vector2D = Vector2D.Zero
    var isFiringPrimary: Boolean = false
    var isTriggeringSecondary: Boolean = false

    private var primaryFireTimer: Float = 0f
    private var secondaryCooldownTimer: Float = 0f
    private var cameraShakeAmount: Float = 0f
    private var screenFlashTimer: Float = 0f
    private var screenFlashColor: Long? = null

    var totalScore: Int = 0
    var sessionCredits: Int = 0
    var sessionExoticMatter: Int = 0
    var isAccomplished: Boolean = false
    var isDefeated: Boolean = false

    val combatEvents = mutableListOf<CombatLogEvent>()
    var elapsedMissionTime: Float = 0f
    private var lastCrashWarningLogged: Boolean = false
    private var lastStableOrbitLogged: Boolean = false
    private var lastShieldDepletedLogged: Boolean = false
    private var lastHullCriticalLogged: Boolean = false

    fun logCombatEvent(message: String, type: CombatEventType, color: Long = type.defaultColor) {
        val totalSecs = elapsedMissionTime.toInt()
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        val timeLabel = String.format("T+%02d:%02d", mins, secs)

        val event = CombatLogEvent(
            id = System.nanoTime(),
            timestampSeconds = elapsedMissionTime,
            timeLabel = timeLabel,
            message = message,
            type = type,
            color = color
        )
        combatEvents.add(event)
        if (combatEvents.size > 80) {
            combatEvents.removeAt(0)
        }
    }

    fun divertPower(system: PowerSystem) {
        powerSystem = system
        val modeName = when (system) {
            PowerSystem.BALANCED -> "EQUILIBRADO"
            PowerSystem.WEAPONS_FOCUS -> "SOBRECARGA DE ARMAS"
            PowerSystem.SHIELDS_FOCUS -> "REFORZAR ESCUDOS"
            PowerSystem.ENGINES_FOCUS -> "IMPULSO A MOTORES"
        }
        logCombatEvent("REACTOR: Energía reconducida a $modeName", CombatEventType.SYSTEM_DIVERT)
    }

    fun clearCombatLog() {
        combatEvents.clear()
        logCombatEvent("CONSOLA TÁCTICA: Registro reiniciado", CombatEventType.TACTICAL_INFO)
    }

    fun endMission() {
        audioEngine.resetIntensity()
        isFiringPrimary = false
        isTriggeringSecondary = false
        thrustVector = Vector2D.Zero
    }

    fun startMission(sector: CampaignSector, customization: ShipCustomization) {
        currentSector = sector
        playerCustomization = customization
        audioEngine.isBossBattle = false
        audioEngine.combatIntensity = 0.2f

        // Reset game state
        enemies.clear()
        projectiles.clear()
        particles.clear()
        shockwaves.clear()
        salvages.clear()
        celestialBodies.clear()

        isAccomplished = false
        isDefeated = false
        totalScore = 0
        sessionCredits = 0
        sessionExoticMatter = 0
        cameraShakeAmount = 0f
        screenFlashColor = null

        // Configure celestial bodies for sector
        setupSectorBodies(sector.id)

        // Initialize player ship with customization
        val maxHull = customization.totalMaxHull
        val maxShield = customization.totalMaxShield
        val maxEnergy = customization.totalMaxEnergy

        val startOrbitRadius = celestialBodies.firstOrNull()?.let { it.radius + 360f } ?: 500f
        val orbitalSpeed = celestialBodies.firstOrNull()?.let {
            sqrt((physicsEngine.gravitationalConstant * it.mass) / startOrbitRadius)
        } ?: 300f

        playerShip = SpaceEntity(
            id = 1L,
            position = Vector2D(0f, -startOrbitRadius),
            velocity = Vector2D(orbitalSpeed, 0f),
            heading = 0f,
            hull = maxHull,
            maxHull = maxHull,
            shield = maxShield,
            maxShield = maxShield,
            energy = maxEnergy,
            maxEnergy = maxEnergy,
            shipName = customization.shipName,
            isPlayer = true,
            radius = when (customization.shipClass) {
                ShipClass.ARTEMIS_SCOUT -> 22f
                ShipClass.ORION_CRUISER -> 28f
                ShipClass.PROMETHEUS_MOTHERSHIP -> 38f
                ShipClass.DAEDALUS_TITAN -> 46f
            },
            color = customization.paintColor
        )

        // Spawn initial enemy fleet around orbit
        spawnEnemyFleet(sector)

        combatEvents.clear()
        elapsedMissionTime = 0f
        lastCrashWarningLogged = false
        lastStableOrbitLogged = false
        lastShieldDepletedLogged = false
        lastHullCriticalLogged = false

        logCombatEvent("MISIÓN INICIADA: Despliegue orbital en ${sector.name}", CombatEventType.TACTICAL_INFO)
        logCombatEvent("NAVE INSIGNIA: ${customization.shipName} [${customization.shipClass.displayName}] activa", CombatEventType.TACTICAL_INFO)
        logCombatEvent("ORDEN OPERACIONAL: ${sector.primaryTarget}", CombatEventType.TACTICAL_INFO)
        logCombatEvent("AMENAZA CONFIRMADA: ${sector.bossLeader.name} al mando de ${sector.bossLeader.flagshipName}", CombatEventType.BOSS_THREAT)
    }

    private fun setupSectorBodies(sectorId: Int) {
        when (sectorId) {
            1 -> {
                // Earth
                celestialBodies.add(
                    CelestialBody(
                        id = "earth",
                        name = "La Tierra",
                        position = Vector2D(0f, 0f),
                        radius = 240f,
                        mass = 120f,
                        baseColor = 0xFF1976D2,
                        atmosphereColor = 0xFF80D8FF
                    )
                )
                // Moon
                celestialBodies.add(
                    CelestialBody(
                        id = "moon",
                        name = "La Luna",
                        position = Vector2D(2200f, -600f),
                        radius = 90f,
                        mass = 25f,
                        baseColor = 0xFFB0BEC5,
                        atmosphereColor = 0x22FFFFFF
                    )
                )
            }
            2 -> {
                // Mars
                celestialBodies.add(
                    CelestialBody(
                        id = "mars",
                        name = "Marte",
                        position = Vector2D(0f, 0f),
                        radius = 210f,
                        mass = 95f,
                        baseColor = 0xFFD84315,
                        atmosphereColor = 0xFFFFAB91
                    )
                )
                // Phobos
                celestialBodies.add(
                    CelestialBody(
                        id = "phobos",
                        name = "Fobos",
                        position = Vector2D(800f, 400f),
                        radius = 65f,
                        mass = 15f,
                        baseColor = 0xFF5D4037,
                        atmosphereColor = 0x11FFFFFF
                    )
                )
            }
            3 -> {
                // Ceres and Asteroid Core
                celestialBodies.add(
                    CelestialBody(
                        id = "ceres",
                        name = "Planeta Enano Ceres",
                        position = Vector2D(0f, 0f),
                        radius = 160f,
                        mass = 60f,
                        baseColor = 0xFF78909C,
                        atmosphereColor = 0x33B0BEC5,
                        hasRings = true,
                        ringRadius = 450f
                    )
                )
            }
            4 -> {
                // Jupiter & Europa
                celestialBodies.add(
                    CelestialBody(
                        id = "jupiter",
                        name = "Júpiter",
                        position = Vector2D(-600f, -800f),
                        radius = 420f,
                        mass = 350f,
                        baseColor = 0xFFBF360C,
                        atmosphereColor = 0xFFFFCC80
                    )
                )
                celestialBodies.add(
                    CelestialBody(
                        id = "europa",
                        name = "Luna Europa",
                        position = Vector2D(0f, 0f),
                        radius = 170f,
                        mass = 75f,
                        baseColor = 0xFF81D4FA,
                        atmosphereColor = 0xFFB3E5FC
                    )
                )
            }
            5 -> {
                // Saturn and Wormhole
                celestialBodies.add(
                    CelestialBody(
                        id = "saturn",
                        name = "Saturno",
                        position = Vector2D(0f, 0f),
                        radius = 320f,
                        mass = 240f,
                        baseColor = 0xFFF57F17,
                        atmosphereColor = 0xFFFFE082,
                        hasRings = true,
                        ringRadius = 680f
                    )
                )
                celestialBodies.add(
                    CelestialBody(
                        id = "anomaly",
                        name = "Vórtice Cuántico",
                        position = Vector2D(1500f, -1200f),
                        radius = 110f,
                        mass = 180f,
                        baseColor = 0xFF12005E,
                        atmosphereColor = 0xFFD500F9,
                        isBlackHole = true
                    )
                )
            }
        }
    }

    private fun spawnEnemyFleet(sector: CampaignSector) {
        val count = sector.enemyFleetSize
        val leader = sector.bossLeader

        for (i in 0 until count) {
            val angle = (2f * PI.toFloat() / count) * i + 0.3f
            val distance = Random.nextFloat() * 600f + 850f
            val pos = Vector2D.fromAngle(angle, distance)
            val isCruiser = i % 3 == 0

            val health = if (isCruiser) 450f else 220f
            val shield = if (isCruiser) 320f else 150f

            enemies.add(
                SpaceEntity(
                    id = 100L + i,
                    position = pos,
                    velocity = Vector2D.fromAngle(angle + PI.toFloat() / 2f, 180f),
                    heading = angle + PI.toFloat(),
                    hull = health,
                    maxHull = health,
                    shield = shield,
                    maxShield = shield,
                    energy = 100f,
                    maxEnergy = 100f,
                    shipName = if (isCruiser) "Crucero ${leader.faction}" else "Caza ${leader.faction}",
                    radius = if (isCruiser) 28f else 18f,
                    color = leader.portraitColor,
                    scoreReward = if (isCruiser) 250 else 100,
                    creditsReward = if (isCruiser) 120 else 50,
                    exoticMatterReward = if (isCruiser) 12 else 4
                )
            )
        }

        // Spawn Boss flagship
        val bossPos = Vector2D(0f, 1400f)
        enemies.add(
            SpaceEntity(
                id = 999L,
                position = bossPos,
                velocity = Vector2D(-120f, 0f),
                heading = -PI.toFloat() / 2f,
                hull = leader.maxHealth,
                maxHull = leader.maxHealth,
                shield = leader.maxHealth * 0.7f,
                maxShield = leader.maxHealth * 0.7f,
                energy = 500f,
                maxEnergy = 500f,
                shipName = leader.flagshipName,
                isBoss = true,
                radius = 52f,
                color = leader.portraitColor,
                scoreReward = 3000,
                creditsReward = sector.rewardCredits,
                exoticMatterReward = sector.rewardExoticMatter
            )
        )
    }

    fun update(dt: Float) {
        if (isGameOver() || isAccomplished) return

        val clampedDt = dt.coerceIn(0.001f, 0.05f)
        elapsedMissionTime += clampedDt

        // 1. UPDATE TIMERS & COOLDOWNS
        primaryFireTimer -= clampedDt
        if (secondaryCooldownTimer > 0f) {
            secondaryCooldownTimer = max(0f, secondaryCooldownTimer - clampedDt)
        }
        if (cameraShakeAmount > 0f) {
            cameraShakeAmount = max(0f, cameraShakeAmount - clampedDt * 2.5f)
        }
        if (screenFlashTimer > 0f) {
            screenFlashTimer -= clampedDt
            if (screenFlashTimer <= 0f) screenFlashColor = null
        }

        // 2. PLAYER POWER SYSTEM BONUSES
        val (energyRechargeMult, shieldRechargeMult, engineSpeedMult) = when (powerSystem) {
            PowerSystem.BALANCED -> Triple(1.0f, 1.0f, 1.0f)
            PowerSystem.WEAPONS_FOCUS -> Triple(1.6f, 0.7f, 0.85f)
            PowerSystem.SHIELDS_FOCUS -> Triple(0.8f, 2.0f, 0.85f)
            PowerSystem.ENGINES_FOCUS -> Triple(0.8f, 0.6f, 1.45f)
        }

        // Regenerate player shield and energy
        playerShip.energy = min(
            playerShip.maxEnergy,
            playerShip.energy + playerCustomization.reactor.rechargeRate * energyRechargeMult * clampedDt
        )
        if (playerShip.shield < playerShip.maxShield) {
            playerShip.shield = min(
                playerShip.maxShield,
                playerShip.shield + 22f * shieldRechargeMult * clampedDt
            )
        }

        // 3. PLAYER CONTROLS & ORBITAL PHYSICS
        // Apply gravity from all bodies
        var totalGravity = Vector2D.Zero
        for (body in celestialBodies) {
            totalGravity = totalGravity + body.calculateGravityOn(playerShip.position)
        }
        playerShip.velocity = playerShip.velocity + (totalGravity * clampedDt)

        // Apply Player Thrust Vector
        if (thrustVector.lengthSquared() > 0.01f) {
            val thrustPower = playerCustomization.totalSpeed * engineSpeedMult
            val appliedThrust = thrustVector * thrustPower
            playerShip.velocity = playerShip.velocity + (appliedThrust * clampedDt)

            // Turn player toward thrust or stick vector
            val targetAngle = thrustVector.angle()
            playerShip.heading = targetAngle

            // Thruster particles
            physicsEngine.spawnThrusterParticles(
                playerShip.position,
                playerShip.heading,
                playerShip.velocity,
                particles,
                count = 2,
                color = playerCustomization.paintColor
            )
        } else if (flightAssistEnabled) {
            // Orbital stabilizer dampens perpendicular unwanted drift slightly
            val speed = playerShip.velocity.length()
            if (speed > 800f) {
                playerShip.velocity = playerShip.velocity * (1f - 0.1f * clampedDt)
            }
        }

        // Update player position
        playerShip.position = playerShip.position + (playerShip.velocity * clampedDt)

        // Check crash into celestial bodies
        for (body in celestialBodies) {
            val dist = playerShip.position.distanceTo(body.position)
            if (dist < body.radius + playerShip.radius) {
                // High gravity crash impact!
                playerShip.hull = 0f
                isDefeated = true
                physicsEngine.spawnExplosionParticles(playerShip.position, particles, shockwaves, true)
                audioEngine.playSfx(SfxType.EXPLOSION_BOSS)
                onVibratePattern?.invoke(HapticPattern.EXPLOSION_HEAVY)
                logCombatEvent("COLISIÓN FATAL: Impacto catastrófico en ${body.name}", CombatEventType.HULL_CRITICAL, 0xFFFF1744)
                break
            }
        }

        // 4. WEAPONS FIRING
        if (isFiringPrimary && primaryFireTimer <= 0f) {
            val weapon = playerCustomization.primaryWeapon
            val energyNeeded = weapon.energyCost * (if (powerSystem == PowerSystem.WEAPONS_FOCUS) 0.8f else 1.0f)

            if (playerShip.energy >= energyNeeded) {
                playerShip.energy -= energyNeeded
                primaryFireTimer = 1f / weapon.fireRate

                // Spawn projectile(s)
                val spreadAngles = when (weapon) {
                    WeaponType.DUAL_GAUSS -> listOf(-0.06f, 0.06f)
                    else -> listOf(0f)
                }

                for (spread in spreadAngles) {
                    val fireAngle = playerShip.heading + spread
                    val muzzleOffset = Vector2D.fromAngle(fireAngle, playerShip.radius + 12f)
                    val projVel = Vector2D.fromAngle(fireAngle, weapon.projectileSpeed) + playerShip.velocity

                    projectiles.add(
                        Projectile(
                            id = System.nanoTime(),
                            position = playerShip.position + muzzleOffset,
                            velocity = projVel,
                            damage = weapon.damage * (if (powerSystem == PowerSystem.WEAPONS_FOCUS) 1.25f else 1.0f),
                            color = weapon.color,
                            isEnemy = false,
                            radius = if (weapon == WeaponType.HEAVY_PLASMA) 7f else 4f
                        )
                    )
                }

                audioEngine.playSfx(if (weapon == WeaponType.HEAVY_PLASMA) SfxType.PLASMA_BLAST else SfxType.LASER_FIRE)
                onVibratePattern?.invoke(HapticPattern.LIGHT_SHOT)
            }
        }

        // Secondary tactical weapon trigger
        if (isTriggeringSecondary && secondaryCooldownTimer <= 0f) {
            val ordnance = playerCustomization.secondaryOrdnance
            if (playerShip.energy >= ordnance.energyCost) {
                playerShip.energy -= ordnance.energyCost
                secondaryCooldownTimer = ordnance.cooldown
                isTriggeringSecondary = false
                logCombatEvent("DISPARO TÁCTICO: ${ordnance.displayName} activado", CombatEventType.WEAPONS_FIRE)

                when (ordnance) {
                    OrdnanceType.PHOTON_TORPEDO -> {
                        val muzzle = Vector2D.fromAngle(playerShip.heading, playerShip.radius + 16f)
                        projectiles.add(
                            Projectile(
                                id = System.nanoTime(),
                                position = playerShip.position + muzzle,
                                velocity = Vector2D.fromAngle(playerShip.heading, 650f) + playerShip.velocity,
                                damage = ordnance.damage,
                                color = 0xFFFF6D00,
                                isEnemy = false,
                                isHoming = true,
                                targetId = lockedTargetId,
                                isTorpedo = true,
                                radius = 9f
                            )
                        )
                        audioEngine.playSfx(SfxType.TORPEDO_LAUNCH)
                        onVibratePattern?.invoke(HapticPattern.ORDNANCE_LAUNCH)
                    }
                    OrdnanceType.EMP_NOVA -> {
                        shockwaves.add(
                            Shockwave(
                                position = playerShip.position,
                                radius = 10f,
                                maxRadius = 380f,
                                alpha = 1.0f,
                                color = 0xFF00E5FF,
                                growthRate = 500f
                            )
                        )
                        audioEngine.playSfx(SfxType.EMP_PULSE)
                        // Damage & stun all nearby enemies
                        for (enemy in enemies) {
                            if (enemy.position.distanceTo(playerShip.position) < 380f) {
                                enemy.shield = max(0f, enemy.shield - 200f)
                                enemy.hull = max(0f, enemy.hull - 120f)
                                enemy.shieldHitFlash = 1f
                            }
                        }
                    }
                    OrdnanceType.MICRO_MISSILES -> {
                        for (i in -3..3) {
                            val angle = playerShip.heading + (i * 0.12f)
                            projectiles.add(
                                Projectile(
                                    id = System.nanoTime() + i,
                                    position = playerShip.position + Vector2D.fromAngle(angle, 25f),
                                    velocity = Vector2D.fromAngle(angle, 780f) + playerShip.velocity,
                                    damage = 60f,
                                    color = 0xFFFFB300,
                                    isEnemy = false,
                                    isHoming = true,
                                    radius = 5f
                                )
                            )
                        }
                        audioEngine.playSfx(SfxType.TORPEDO_LAUNCH)
                    }
                    OrdnanceType.REPAIR_NANITES -> {
                        playerShip.hull = min(playerShip.maxHull, playerShip.hull + 450f)
                        playerShip.shield = playerShip.maxShield
                        audioEngine.playSfx(SfxType.SHIELD_HIT)
                        triggerScreenFlash(0xFF00E676)
                    }
                }
            }
        }

        // 5. UPDATE ENEMIES & AI
        val enemyIterator = enemies.iterator()
        while (enemyIterator.hasNext()) {
            val enemy = enemyIterator.next()
            if (enemy.hull <= 0f) {
                // Enemy destroyed!
                physicsEngine.spawnExplosionParticles(enemy.position, particles, shockwaves, enemy.isBoss)
                audioEngine.playSfx(if (enemy.isBoss) SfxType.EXPLOSION_BOSS else SfxType.EXPLOSION_SMALL)
                cameraShakeAmount = if (enemy.isBoss) 24f else 8f
                onVibratePattern?.invoke(if (enemy.isBoss) HapticPattern.EXPLOSION_HEAVY else HapticPattern.EXPLOSION_MEDIUM)

                totalScore += enemy.scoreReward
                sessionCredits += enemy.creditsReward
                sessionExoticMatter += enemy.exoticMatterReward

                logCombatEvent(
                    if (enemy.isBoss) "¡OBJETIVO CLAVE DESTRUIDO! ${enemy.shipName} (+${enemy.creditsReward} CR, +${enemy.exoticMatterReward} MT)"
                    else "BLANCO ELIMINADO: ${enemy.shipName} (+${enemy.creditsReward} CR)",
                    if (enemy.isBoss) CombatEventType.BOSS_THREAT else CombatEventType.ENEMY_DESTROYED,
                    if (enemy.isBoss) 0xFFFF1744 else 0xFF00E676
                )

                // Drop salvage
                salvages.add(
                    FloatingSalvage(
                        id = System.nanoTime(),
                        position = enemy.position,
                        velocity = enemy.velocity * 0.3f,
                        type = if (Random.nextFloat() > 0.4f) SalvageType.CREDITS_CONTAINER else SalvageType.EXOTIC_MATTER,
                        value = enemy.creditsReward
                    )
                )

                enemyIterator.remove()
                continue
            }

            // Apply gravity to enemy
            for (body in celestialBodies) {
                val g = body.calculateGravityOn(enemy.position)
                enemy.velocity = enemy.velocity + (g * clampedDt)
            }

            // Enemy AI Combat logic
            val toPlayer = playerShip.position - enemy.position
            val distToPlayer = toPlayer.length()
            val desiredAngle = toPlayer.angle()
            enemy.heading = desiredAngle

            if (distToPlayer > 350f) {
                // Close in
                val thrust = toPlayer.normalized() * (if (enemy.isBoss) 190f else 260f)
                enemy.velocity = enemy.velocity + (thrust * clampedDt)
            } else {
                // Strafe around
                val strafeAngle = desiredAngle + PI.toFloat() / 2f
                val strafeThrust = Vector2D.fromAngle(strafeAngle, 140f)
                enemy.velocity = enemy.velocity + (strafeThrust * clampedDt)
            }

            enemy.position = enemy.position + (enemy.velocity * clampedDt)

            // Enemy weapon fire
            enemy.attackCooldown -= clampedDt
            val fireThreshold = if (enemy.isBoss) 0.65f else 1.4f
            if (enemy.attackCooldown <= 0f && distToPlayer < 900f) {
                enemy.attackCooldown = fireThreshold + Random.nextFloat() * 0.5f

                val muzzle = enemy.position + Vector2D.fromAngle(enemy.heading, enemy.radius + 10f)
                val projVel = Vector2D.fromAngle(enemy.heading, 650f) + enemy.velocity

                projectiles.add(
                    Projectile(
                        id = System.nanoTime(),
                        position = muzzle,
                        velocity = projVel,
                        damage = if (enemy.isBoss) 45f else 24f,
                        color = enemy.color,
                        isEnemy = true,
                        radius = if (enemy.isBoss) 6f else 4f
                    )
                )
            }

            // Boss special attack
            if (enemy.isBoss) {
                enemy.specialCooldown -= clampedDt
                if (enemy.specialCooldown <= 0f && distToPlayer < 1200f) {
                    enemy.specialCooldown = 7.0f
                    logCombatEvent("⚠️ ALERTA JEFE: ${enemy.shipName} lanzó ${currentSector.bossLeader.specialAttack}", CombatEventType.BOSS_THREAT, 0xFFFF5252)
                    // Launch boss multi-burst
                    for (k in -2..2) {
                        val angle = enemy.heading + (k * 0.22f)
                        projectiles.add(
                            Projectile(
                                id = System.nanoTime() + k,
                                position = enemy.position + Vector2D.fromAngle(angle, enemy.radius + 15f),
                                velocity = Vector2D.fromAngle(angle, 540f),
                                damage = 65f,
                                color = 0xFFFF1744,
                                isEnemy = true,
                                isHoming = (k == 0),
                                radius = 7f
                            )
                        )
                    }
                    audioEngine.playSfx(SfxType.TORPEDO_LAUNCH)
                }
            }

            // Hit flash decays
            if (enemy.shieldHitFlash > 0f) enemy.shieldHitFlash = max(0f, enemy.shieldHitFlash - clampedDt * 4f)
            if (enemy.hullHitFlash > 0f) enemy.hullHitFlash = max(0f, enemy.hullHitFlash - clampedDt * 4f)
        }

        // 6. UPDATE PROJECTILES & COLLISION DETECTION
        val projIterator = projectiles.iterator()
        while (projIterator.hasNext()) {
            val proj = projIterator.next()
            proj.lifetime -= clampedDt
            if (proj.lifetime <= 0f) {
                projIterator.remove()
                continue
            }

            // Homing logic
            if (proj.isHoming) {
                val targetPos = if (proj.isEnemy) {
                    playerShip.position
                } else {
                    enemies.firstOrNull { it.id == proj.targetId }?.position
                        ?: enemies.minByOrNull { it.position.distanceTo(proj.position) }?.position
                }

                if (targetPos != null) {
                    val toTarget = (targetPos - proj.position).normalized()
                    val curHeading = proj.velocity.normalized()
                    val blended = (curHeading * 0.92f + toTarget * 0.08f).normalized()
                    proj.velocity = blended * proj.velocity.length()
                }
            }

            proj.position = proj.position + (proj.velocity * clampedDt)

            // Hit detection
            if (proj.isEnemy) {
                // Check hit player
                val dist = proj.position.distanceTo(playerShip.position)
                if (dist < playerShip.radius + proj.radius) {
                    // Damage player
                    val actualDamage = proj.damage * (1f - playerCustomization.armor.damageReduction)
                    if (playerShip.shield > 0f) {
                        val shieldDamage = min(playerShip.shield, actualDamage)
                        playerShip.shield -= shieldDamage
                        val leftover = actualDamage - shieldDamage
                        if (leftover > 0f) playerShip.hull = max(0f, playerShip.hull - leftover)
                        audioEngine.playSfx(SfxType.SHIELD_HIT)
                        triggerScreenFlash(0x8000E5FF)
                    } else {
                        playerShip.hull = max(0f, playerShip.hull - actualDamage)
                        audioEngine.playSfx(SfxType.EXPLOSION_SMALL)
                        triggerScreenFlash(0x80FF1744)
                        cameraShakeAmount = 14f
                        onVibratePattern?.invoke(HapticPattern.KINETIC_IMPACT)
                    }

                    if (playerShip.shield <= 0f && !lastShieldDepletedLogged) {
                        logCombatEvent("🚨 ¡ESCUDOS AGOTADOS! Casco vulnerable", CombatEventType.SHIELD_WARNING, 0xFF2979FF)
                        onVibratePattern?.invoke(HapticPattern.SHIELD_COLLAPSE)
                        lastShieldDepletedLogged = true
                    } else if (playerShip.shield > 60f) {
                        lastShieldDepletedLogged = false
                    }

                    if (playerShip.hull < playerShip.maxHull * 0.25f && !lastHullCriticalLogged) {
                        logCombatEvent("⚠️ CASCO CRÍTICO: Integridad inferior al 25%", CombatEventType.HULL_CRITICAL, 0xFFFF1744)
                        onVibratePattern?.invoke(HapticPattern.CRITICAL_ALARM)
                        lastHullCriticalLogged = true
                    }

                    if (playerShip.hull <= 0f) {
                        isDefeated = true
                        physicsEngine.spawnExplosionParticles(playerShip.position, particles, shockwaves, true)
                        audioEngine.playSfx(SfxType.EXPLOSION_BOSS)
                        onVibratePattern?.invoke(HapticPattern.EXPLOSION_HEAVY)
                        logCombatEvent("💥 NAVE CAPITANA PERDIDA: Secuencia de escape iniciada", CombatEventType.HULL_CRITICAL, 0xFFFF1744)
                    }

                    projIterator.remove()
                    continue
                }
            } else {
                // Check hit enemies
                var hitEnemy: SpaceEntity? = null
                for (enemy in enemies) {
                    if (proj.position.distanceTo(enemy.position) < enemy.radius + proj.radius) {
                        hitEnemy = enemy
                        break
                    }
                }

                if (hitEnemy != null) {
                    if (hitEnemy.shield > 0f) {
                        val sDmg = min(hitEnemy.shield, proj.damage)
                        hitEnemy.shield -= sDmg
                        hitEnemy.shieldHitFlash = 1f
                        val leftover = proj.damage - sDmg
                        if (leftover > 0f) hitEnemy.hull = max(0f, hitEnemy.hull - leftover)
                    } else {
                        hitEnemy.hull = max(0f, hitEnemy.hull - proj.damage)
                        hitEnemy.hullHitFlash = 1f
                    }

                    // Impact spark particles
                    for (s in 0..4) {
                        val sparkAngle = Random.nextFloat() * 2f * PI.toFloat()
                        particles.add(
                            Particle(
                                position = proj.position,
                                velocity = Vector2D.fromAngle(sparkAngle, 120f),
                                color = proj.color,
                                size = 3f,
                                life = 1f,
                                decayRate = 4.0f
                            )
                        )
                    }

                    projIterator.remove()
                    continue
                }
            }
        }

        // 7. UPDATE SALVAGE ITEMS & PICKUPS
        val salvageIter = salvages.iterator()
        while (salvageIter.hasNext()) {
            val sal = salvageIter.next()
            sal.position = sal.position + (sal.velocity * clampedDt)
            sal.lifetime -= clampedDt

            // Pull towards player if close (magnetic tractor beam!)
            val dist = sal.position.distanceTo(playerShip.position)
            if (dist < 220f) {
                val pull = (playerShip.position - sal.position).normalized() * 380f
                sal.velocity = sal.velocity + (pull * clampedDt)
            }

            if (dist < playerShip.radius + 15f) {
                // Collected!
                when (sal.type) {
                    SalvageType.CREDITS_CONTAINER -> {
                        sessionCredits += sal.value
                        logCombatEvent("SUMINISTROS: Contenedor de Créditos recuperado (+${sal.value} CR)", CombatEventType.SALVAGE_ACQUIRED)
                    }
                    SalvageType.EXOTIC_MATTER -> {
                        sessionExoticMatter += 8
                        logCombatEvent("RECURSO RARO: Materia Exótica asegurada (+8 MT)", CombatEventType.SALVAGE_ACQUIRED)
                    }
                    SalvageType.HULL_REPAIR -> {
                        playerShip.hull = min(playerShip.maxHull, playerShip.hull + 200f)
                        logCombatEvent("NANOBOTS: Reparación de emergencia (+200 HP Casco)", CombatEventType.SALVAGE_ACQUIRED)
                    }
                    SalvageType.SHIELD_BOOST -> {
                        playerShip.shield = playerShip.maxShield
                        logCombatEvent("GENERADOR: Sobrecarga total de escudos de combate", CombatEventType.SALVAGE_ACQUIRED)
                    }
                }
                audioEngine.playSfx(SfxType.SHIELD_HIT)
                salvageIter.remove()
                continue
            }

            if (sal.lifetime <= 0f) salvageIter.remove()
        }

        // 8. UPDATE PARTICLES & SHOCKWAVES
        val partIter = particles.iterator()
        while (partIter.hasNext()) {
            val p = partIter.next()
            p.position = p.position + (p.velocity * clampedDt)
            p.life -= p.decayRate * clampedDt
            p.alpha = max(0f, p.life)
            if (p.isDebris) {
                p.rotation += p.rotationSpeed * clampedDt
            }
            if (p.life <= 0f) partIter.remove()
        }

        val shockIter = shockwaves.iterator()
        while (shockIter.hasNext()) {
            val s = shockIter.next()
            s.radius += s.growthRate * clampedDt
            s.alpha = max(0f, 1f - (s.radius / s.maxRadius))
            if (s.radius >= s.maxRadius || s.alpha <= 0.02f) shockIter.remove()
        }

        // 9. CHECK MISSION COMPLETION
        val bossEntity = enemies.firstOrNull { it.isBoss }
        val allCleared = enemies.isEmpty() || (bossEntity == null && enemies.size <= 1)
        if (allCleared && !isAccomplished) {
            isAccomplished = true
            sessionCredits += currentSector.rewardCredits
            sessionExoticMatter += currentSector.rewardExoticMatter
            totalScore += 5000
            audioEngine.playSfx(SfxType.HYPERSPACE_JUMP)
            onVibratePattern?.invoke(HapticPattern.WARP_ENGAGE)
            logCombatEvent("¡VICTORIA TOTAL! Sector asegurado (+${currentSector.rewardCredits} CR, +${currentSector.rewardExoticMatter} MT)", CombatEventType.TACTICAL_INFO, 0xFF00E676)
        }

        // 10. UPDATE AUDIO ENGINE INTENSITY
        val nearbyThreats = enemies.count { it.position.distanceTo(playerShip.position) < 800f }
        val isBossAlive = (bossEntity != null)
        audioEngine.isBossBattle = isBossAlive
        audioEngine.combatIntensity = if (isBossAlive) 0.9f else (nearbyThreats * 0.15f).coerceIn(0.1f, 0.8f)

        // 11. UPDATE UI TELEMETRY STATE
        val telemetry = physicsEngine.calculateOrbitalTelemetry(
            playerShip.position,
            playerShip.velocity,
            celestialBodies
        )

        if (telemetry.crashWarning && !lastCrashWarningLogged) {
            logCombatEvent("PELIGRO ORBITAL: Altitud crítica respecto a ${telemetry.nearestBodyName}", CombatEventType.ORBITAL_EVENT, 0xFFFF1744)
            lastCrashWarningLogged = true
        } else if (!telemetry.crashWarning) {
            lastCrashWarningLogged = false
        }

        if (telemetry.isStableOrbit && !lastStableOrbitLogged) {
            logCombatEvent("NAVEGACIÓN: Órbita estable calculada alrededor de ${telemetry.nearestBodyName}", CombatEventType.ORBITAL_EVENT, 0xFF00E676)
            lastStableOrbitLogged = true
        } else if (!telemetry.isStableOrbit) {
            lastStableOrbitLogged = false
        }

        // Target lock-on
        val lockedEnemy = enemies.firstOrNull { it.id == lockedTargetId }
            ?: enemies.minByOrNull { it.position.distanceTo(playerShip.position) }

        if (lockedEnemy != null) {
            lockedTargetId = lockedEnemy.id
        } else {
            lockedTargetId = null
        }

        _uiState.value = GameUIState(
            playerHull = playerShip.hull,
            playerMaxHull = playerShip.maxHull,
            playerShield = playerShip.shield,
            playerMaxShield = playerShip.maxShield,
            playerEnergy = playerShip.energy,
            playerMaxEnergy = playerShip.maxEnergy,
            speedKmh = telemetry.orbitalSpeed * 36f, // scaled to display km/h or km/s
            altitudeKm = telemetry.altitude,
            nearestBody = telemetry.nearestBodyName,
            isStableOrbit = telemetry.isStableOrbit,
            crashWarning = telemetry.crashWarning,
            lockedTargetName = lockedEnemy?.shipName,
            lockedTargetDistance = lockedEnemy?.let { it.position.distanceTo(playerShip.position) } ?: 0f,
            lockedTargetHullPercent = lockedEnemy?.let { it.hull / it.maxHull } ?: 0f,
            enemiesRemaining = enemies.size,
            bossActive = isBossAlive,
            bossName = bossEntity?.shipName ?: "",
            bossHealthPercent = bossEntity?.let { it.hull / it.maxHull } ?: 0f,
            currentScore = totalScore,
            creditsEarned = sessionCredits,
            exoticMatterEarned = sessionExoticMatter,
            isMissionAccomplished = isAccomplished,
            isGameOver = isDefeated,
            powerSystem = powerSystem,
            cameraShake = cameraShakeAmount,
            screenFlashColor = screenFlashColor,
            secondaryCooldownPercent = if (playerCustomization.secondaryOrdnance.cooldown > 0f) {
                secondaryCooldownTimer / playerCustomization.secondaryOrdnance.cooldown
            } else 0f,
            combatLog = combatEvents.toList()
        )
    }

    private fun triggerScreenFlash(color: Long) {
        screenFlashColor = color
        screenFlashTimer = 0.18f
    }

    fun isGameOver(): Boolean = isDefeated
}
