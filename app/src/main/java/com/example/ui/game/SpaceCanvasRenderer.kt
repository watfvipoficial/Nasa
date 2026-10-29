package com.example.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.engine.*
import com.example.game.GameManager
import com.example.model.ShipClass
import kotlin.math.*
import kotlin.random.Random

class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val brightness: Float,
    val color: Color
)

@Composable
fun SpaceCanvasRenderer(
    gameManager: GameManager,
    modifier: Modifier = Modifier
) {
    // Generate persistent starfield
    val stars = remember {
        val list = mutableListOf<Star>()
        val starColors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFF80D8FF),
            Color(0xFFFFD54F),
            Color(0xFFB388FF),
            Color(0xFF80CBC4)
        )
        val rng = Random(42)
        for (i in 0..240) {
            list.add(
                Star(
                    x = rng.nextFloat() * 4000f - 2000f,
                    y = rng.nextFloat() * 4000f - 2000f,
                    size = rng.nextFloat() * 2.5f + 1f,
                    brightness = rng.nextFloat() * 0.7f + 0.3f,
                    color = starColors[rng.nextInt(starColors.size)]
                )
            )
        }
        list
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

        // Camera is centered on player ship with camera shake
        val shake = gameManager.uiState.value.cameraShake
        val shakeOffsetX = (Random.nextFloat() - 0.5f) * shake * 2.5f
        val shakeOffsetY = (Random.nextFloat() - 0.5f) * shake * 2.5f

        val playerPos = gameManager.playerShip.position
        val cameraOffset = Offset(
            center.x - playerPos.x + shakeOffsetX,
            center.y - playerPos.y + shakeOffsetY
        )

        // 1. RENDER PARALLAX STARFIELD
        drawStarfield(stars, playerPos, canvasWidth, canvasHeight)

        // 2. RENDER CELESTIAL BODIES
        for (body in gameManager.celestialBodies) {
            val bodyScreenPos = Offset(body.position.x + cameraOffset.x, body.position.y + cameraOffset.y)
            drawCelestialBody(body, bodyScreenPos)
        }

        // 3. RENDER ORBITAL PREDICTION VECTOR
        drawOrbitalPath(gameManager, cameraOffset)

        // 4. RENDER SALVAGE PICKUPS
        for (sal in gameManager.salvages) {
            val sPos = Offset(sal.position.x + cameraOffset.x, sal.position.y + cameraOffset.y)
            drawCircle(
                color = Color(0xFFFFB300),
                radius = 7f,
                center = sPos
            )
            drawCircle(
                color = Color(0x66FFB300),
                radius = 14f,
                center = sPos,
                style = Stroke(width = 2f)
            )
        }

        // 5. RENDER SHOCKWAVES
        for (s in gameManager.shockwaves) {
            val sPos = Offset(s.position.x + cameraOffset.x, s.position.y + cameraOffset.y)
            drawCircle(
                color = Color(s.color).copy(alpha = s.alpha),
                radius = s.radius,
                center = sPos,
                style = Stroke(width = 4f)
            )
        }

        // 6. RENDER PARTICLES
        for (p in gameManager.particles) {
            val pPos = Offset(p.position.x + cameraOffset.x, p.position.y + cameraOffset.y)
            if (p.isDebris) {
                rotate(p.rotation, pPos) {
                    drawRect(
                        color = Color(p.color).copy(alpha = p.alpha),
                        topLeft = Offset(pPos.x - p.size / 2f, pPos.y - p.size / 2f),
                        size = Size(p.size, p.size * 1.8f)
                    )
                }
            } else {
                drawCircle(
                    color = Color(p.color).copy(alpha = p.alpha),
                    radius = p.size,
                    center = pPos
                )
            }
        }

        // 7. RENDER PROJECTILES
        for (proj in gameManager.projectiles) {
            val projPos = Offset(proj.position.x + cameraOffset.x, proj.position.y + cameraOffset.y)
            val velNorm = proj.velocity.normalized()
            val trailStart = projPos - Offset(velNorm.x * 18f, velNorm.y * 18f)

            // Outer glow line
            drawLine(
                color = Color(proj.color).copy(alpha = 0.6f),
                start = trailStart,
                end = projPos,
                strokeWidth = proj.radius * 2f,
                cap = StrokeCap.Round
            )
            // Hot white core
            drawLine(
                color = Color.White,
                start = trailStart * 0.4f + projPos * 0.6f,
                end = projPos,
                strokeWidth = max(2f, proj.radius * 0.8f),
                cap = StrokeCap.Round
            )
        }

        // 8. RENDER ENEMIES & BOSSES
        for (enemy in gameManager.enemies) {
            val ePos = Offset(enemy.position.x + cameraOffset.x, enemy.position.y + cameraOffset.y)
            drawEnemyShip(enemy, ePos)

            // Draw target lock brackets if selected
            if (enemy.id == gameManager.lockedTargetId) {
                drawTargetBrackets(ePos, enemy.radius + 14f)
            }
        }

        // 9. RENDER PLAYER SHIP
        val playerScreenPos = Offset(center.x + shakeOffsetX, center.y + shakeOffsetY)
        drawPlayerShip(gameManager, playerScreenPos)

        // 10. SCREEN DAMAGE / SHIELD FLASH
        val flashColor = gameManager.uiState.value.screenFlashColor
        if (flashColor != null) {
            drawRect(
                color = Color(flashColor).copy(alpha = 0.28f),
                topLeft = Offset.Zero,
                size = size
            )
        }
    }
}

private fun DrawScope.drawStarfield(
    stars: List<Star>,
    playerPos: Vector2D,
    width: Float,
    height: Float
) {
    for (s in stars) {
        // Parallax wrap
        val px = (s.x - playerPos.x * 0.15f).mod(width + 200f) - 100f
        val py = (s.y - playerPos.y * 0.15f).mod(height + 200f) - 100f
        drawCircle(
            color = s.color.copy(alpha = s.brightness),
            radius = s.size,
            center = Offset(px, py)
        )
    }
}

private fun DrawScope.drawCelestialBody(body: CelestialBody, screenPos: Offset) {
    // Atmosphere Halo
    drawCircle(
        color = Color(body.atmosphereColor).copy(alpha = 0.25f),
        radius = body.radius * 1.25f,
        center = screenPos
    )
    drawCircle(
        color = Color(body.atmosphereColor).copy(alpha = 0.12f),
        radius = body.radius * 1.5f,
        center = screenPos
    )

    // Body Core
    drawCircle(
        color = Color(body.baseColor),
        radius = body.radius,
        center = screenPos
    )

    // Planetary Rings
    if (body.hasRings) {
        drawCircle(
            color = Color(0x66FFCC80),
            radius = body.ringRadius,
            center = screenPos,
            style = Stroke(width = 38f)
        )
        drawCircle(
            color = Color(0x99FFE082),
            radius = body.ringRadius * 0.92f,
            center = screenPos,
            style = Stroke(width = 12f)
        )
    }

    // Black Hole Accretion Disk
    if (body.isBlackHole) {
        drawCircle(
            color = Color(0xFFD500F9).copy(alpha = 0.45f),
            radius = body.radius * 1.8f,
            center = screenPos,
            style = Stroke(width = 24f)
        )
        drawCircle(
            color = Color.Black,
            radius = body.radius,
            center = screenPos
        )
    }
}

private fun DrawScope.drawOrbitalPath(gameManager: GameManager, cameraOffset: Offset) {
    val nearest = gameManager.celestialBodies.firstOrNull() ?: return
    val bodyPos = Offset(nearest.position.x + cameraOffset.x, nearest.position.y + cameraOffset.y)
    val playerPos = Offset(gameManager.playerShip.position.x + cameraOffset.x, gameManager.playerShip.position.y + cameraOffset.y)
    val dist = (playerPos - bodyPos).getDistance()

    // Draw orbital ring guide
    drawCircle(
        color = Color(0x2200E5FF),
        radius = dist,
        center = bodyPos,
        style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f)))
    )

    // Draw velocity vector line
    val vel = gameManager.playerShip.velocity
    val speed = vel.length()
    if (speed > 10f) {
        val velEnd = playerPos + Offset(vel.x, vel.y) * 0.35f
        drawLine(
            color = Color(0xFF00E5FF),
            start = playerPos,
            end = velEnd,
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawPlayerShip(gameManager: GameManager, screenPos: Offset) {
    val player = gameManager.playerShip
    val headingDegrees = Math.toDegrees(player.heading.toDouble()).toFloat()
    val shipColor = Color(player.color)
    val shipClass = gameManager.playerCustomization.shipClass

    rotate(headingDegrees + 90f, screenPos) {
        val path = Path()
        val scale = when (shipClass) {
            ShipClass.ARTEMIS_SCOUT -> 1.0f
            ShipClass.ORION_CRUISER -> 1.35f
            ShipClass.PROMETHEUS_MOTHERSHIP -> 1.8f
            ShipClass.DAEDALUS_TITAN -> 2.2f
        }

        // Draw futuristic NASA warship fuselage
        path.moveTo(screenPos.x, screenPos.y - 30f * scale) // Nose
        path.lineTo(screenPos.x + 18f * scale, screenPos.y + 18f * scale) // Right wingtip
        path.lineTo(screenPos.x + 8f * scale, screenPos.y + 12f * scale) // Right inner
        path.lineTo(screenPos.x + 10f * scale, screenPos.y + 26f * scale) // Right thruster
        path.lineTo(screenPos.x - 10f * scale, screenPos.y + 26f * scale) // Left thruster
        path.lineTo(screenPos.x - 8f * scale, screenPos.y + 12f * scale) // Left inner
        path.lineTo(screenPos.x - 18f * scale, screenPos.y + 18f * scale) // Left wingtip
        path.close()

        // Ship main body fill
        drawPath(
            path = path,
            color = Color(0xFF0F1B2E)
        )
        // High-contrast armor edge outline
        drawPath(
            path = path,
            color = shipColor,
            style = Stroke(width = 3f * scale)
        )

        // Cockpit canopy glow
        drawCircle(
            color = Color(0xFF00E5FF),
            radius = 4f * scale,
            center = Offset(screenPos.x, screenPos.y - 10f * scale)
        )

        // Extra Mothership battle structures
        if (shipClass == ShipClass.PROMETHEUS_MOTHERSHIP || shipClass == ShipClass.DAEDALUS_TITAN) {
            drawRect(
                color = Color(0xFFFFB300),
                topLeft = Offset(screenPos.x - 6f * scale, screenPos.y),
                size = Size(12f * scale, 14f * scale)
            )
        }
    }

    // Draw active shield barrier if shield > 0
    if (player.shield > 0f) {
        val shieldPercent = player.shield / player.maxShield
        val shieldAlpha = (0.25f + shieldPercent * 0.35f).coerceIn(0.15f, 0.65f)
        drawCircle(
            color = Color(0xFF00E5FF).copy(alpha = shieldAlpha),
            radius = player.radius + 12f,
            center = screenPos,
            style = Stroke(width = 2.5f)
        )
    }
}

private fun DrawScope.drawEnemyShip(enemy: SpaceEntity, screenPos: Offset) {
    val headingDegrees = Math.toDegrees(enemy.heading.toDouble()).toFloat()
    val enemyColor = Color(enemy.color)

    rotate(headingDegrees + 90f, screenPos) {
        val path = Path()
        val s = if (enemy.isBoss) 2.6f else 1.1f

        if (enemy.isBoss) {
            // Massive Dreadnought design
            path.moveTo(screenPos.x, screenPos.y - 36f * s)
            path.lineTo(screenPos.x + 28f * s, screenPos.y - 10f * s)
            path.lineTo(screenPos.x + 36f * s, screenPos.y + 24f * s)
            path.lineTo(screenPos.x + 12f * s, screenPos.y + 34f * s)
            path.lineTo(screenPos.x - 12f * s, screenPos.y + 34f * s)
            path.lineTo(screenPos.x - 36f * s, screenPos.y + 24f * s)
            path.lineTo(screenPos.x - 28f * s, screenPos.y - 10f * s)
            path.close()
        } else {
            // Interceptor fighter design
            path.moveTo(screenPos.x, screenPos.y - 20f * s)
            path.lineTo(screenPos.x + 14f * s, screenPos.y + 16f * s)
            path.lineTo(screenPos.x, screenPos.y + 10f * s)
            path.lineTo(screenPos.x - 14f * s, screenPos.y + 16f * s)
            path.close()
        }

        drawPath(
            path = path,
            color = Color(0xFF1A0A10)
        )
        drawPath(
            path = path,
            color = if (enemy.hullHitFlash > 0.05f) Color.White else enemyColor,
            style = Stroke(width = 2.5f)
        )

        // Core glow
        drawCircle(
            color = enemyColor,
            radius = if (enemy.isBoss) 10f else 3.5f,
            center = screenPos
        )
    }

    // Health / Shield bar above enemy
    val barWidth = if (enemy.isBoss) 80f else 32f
    val barHeight = if (enemy.isBoss) 6f else 3.5f
    val barY = screenPos.y - enemy.radius - 14f

    // Background bar
    drawRect(
        color = Color(0x80000000),
        topLeft = Offset(screenPos.x - barWidth / 2f, barY),
        size = Size(barWidth, barHeight)
    )
    // Hull bar
    val hullFraction = (enemy.hull / enemy.maxHull).coerceIn(0f, 1f)
    drawRect(
        color = if (hullFraction > 0.35f) Color(0xFF00E676) else Color(0xFFFF1744),
        topLeft = Offset(screenPos.x - barWidth / 2f, barY),
        size = Size(barWidth * hullFraction, barHeight)
    )

    // Shield flash ring
    if (enemy.shieldHitFlash > 0.05f) {
        drawCircle(
            color = Color(0xFF80D8FF).copy(alpha = enemy.shieldHitFlash),
            radius = enemy.radius + 6f,
            center = screenPos,
            style = Stroke(width = 3f)
        )
    }
}

private fun DrawScope.drawTargetBrackets(pos: Offset, radius: Float) {
    val bracketLen = 10f
    val color = Color(0xFFFF1744)
    val stroke = 2.5f

    // Top Left
    drawLine(color, Offset(pos.x - radius, pos.y - radius), Offset(pos.x - radius + bracketLen, pos.y - radius), stroke)
    drawLine(color, Offset(pos.x - radius, pos.y - radius), Offset(pos.x - radius, pos.y - radius + bracketLen), stroke)

    // Top Right
    drawLine(color, Offset(pos.x + radius, pos.y - radius), Offset(pos.x + radius - bracketLen, pos.y - radius), stroke)
    drawLine(color, Offset(pos.x + radius, pos.y - radius), Offset(pos.x + radius, pos.y - radius + bracketLen), stroke)

    // Bottom Left
    drawLine(color, Offset(pos.x - radius, pos.y + radius), Offset(pos.x - radius + bracketLen, pos.y + radius), stroke)
    drawLine(color, Offset(pos.x - radius, pos.y + radius), Offset(pos.x - radius, pos.y + radius - bracketLen), stroke)

    // Bottom Right
    drawLine(color, Offset(pos.x + radius, pos.y + radius), Offset(pos.x + radius - bracketLen, pos.y + radius), stroke)
    drawLine(color, Offset(pos.x + radius, pos.y + radius), Offset(pos.x + radius, pos.y + radius - bracketLen), stroke)
}
