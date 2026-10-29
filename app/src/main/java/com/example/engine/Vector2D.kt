package com.example.engine

import kotlin.math.*

data class Vector2D(val x: Float = 0f, val y: Float = 0f) {
    operator fun plus(other: Vector2D): Vector2D = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D): Vector2D = Vector2D(x - other.x, y - other.y)
    operator fun times(scalar: Float): Vector2D = Vector2D(x * scalar, y * scalar)
    operator fun div(scalar: Float): Vector2D = if (scalar != 0f) Vector2D(x / scalar, y / scalar) else Vector2D()

    fun length(): Float = sqrt(x * x + y * y)
    fun lengthSquared(): Float = x * x + y * y

    fun normalized(): Vector2D {
        val len = length()
        return if (len > 0.0001f) Vector2D(x / len, y / len) else Vector2D(0f, 0f)
    }

    fun distanceTo(other: Vector2D): Float = (this - other).length()

    fun angle(): Float = atan2(y, x)

    fun dot(other: Vector2D): Float = x * other.x + y * other.y

    fun rotated(radians: Float): Vector2D {
        val cos = cos(radians)
        val sin = sin(radians)
        return Vector2D(x * cos - y * sin, x * sin + y * cos)
    }

    companion object {
        val Zero = Vector2D(0f, 0f)
        fun fromAngle(radians: Float, length: Float = 1f): Vector2D {
            return Vector2D(cos(radians) * length, sin(radians) * length)
        }
    }
}
