package com.example.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

enum class HapticPattern {
    UI_TAP,              // Crisp subtle tick for UI tactile confirmation
    LIGHT_SHOT,          // Light recoil for primary cannons
    KINETIC_IMPACT,      // Staccato stutter on hull kinetic damage
    EXPLOSION_MEDIUM,    // Rumble for standard enemy ship destruction
    EXPLOSION_HEAVY,     // Sustained deep tremor for boss destruction or mothership crash
    SHIELD_COLLAPSE,     // Sharp electric shudder when shields deplete
    ORDNANCE_LAUNCH,     // Heavy kickback when launching torpedoes / EMP
    CRITICAL_ALARM,      // Double heartbeat pulse for low hull warning
    WARP_ENGAGE          // Ascending wave for hyperspace jump
}

class HapticController(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var isVibrationEnabled: Boolean = true

    fun trigger(pattern: HapticPattern) {
        if (!isVibrationEnabled) return
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = createVibrationEffect(pattern)
                vib.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(getLegacyPattern(pattern), -1)
            }
        } catch (_: Exception) {
            // Graceful fallback for devices without advanced vibration hardware
        }
    }

    private fun createVibrationEffect(pattern: HapticPattern): VibrationEffect {
        return when (pattern) {
            HapticPattern.UI_TAP -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                } else {
                    VibrationEffect.createOneShot(12, 100)
                }
            }
            HapticPattern.LIGHT_SHOT -> {
                VibrationEffect.createOneShot(25, 90)
            }
            HapticPattern.KINETIC_IMPACT -> {
                // Double tap kinetic strike: pause, hit, pause, heavy hit
                val timings = longArrayOf(0, 35, 30, 65)
                val amplitudes = intArrayOf(0, 180, 0, 255)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
            HapticPattern.EXPLOSION_MEDIUM -> {
                // Expanding explosion shockwave
                val timings = longArrayOf(0, 50, 30, 90, 40, 60)
                val amplitudes = intArrayOf(0, 220, 0, 190, 0, 120)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
            HapticPattern.EXPLOSION_HEAVY -> {
                // Deep lingering boss death rumble
                val timings = longArrayOf(0, 90, 40, 140, 50, 180, 60, 120)
                val amplitudes = intArrayOf(0, 255, 0, 240, 0, 200, 0, 100)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
            HapticPattern.SHIELD_COLLAPSE -> {
                // Sharp high-frequency stinging burst
                val timings = longArrayOf(0, 30, 20, 40, 20, 60)
                val amplitudes = intArrayOf(0, 160, 0, 230, 0, 255)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
            HapticPattern.ORDNANCE_LAUNCH -> {
                // Heavy kickback impulse
                val timings = longArrayOf(0, 95)
                val amplitudes = intArrayOf(0, 230)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
            HapticPattern.CRITICAL_ALARM -> {
                // Heartbeat pulse warning: lub-dub
                val timings = longArrayOf(0, 60, 70, 70)
                val amplitudes = intArrayOf(0, 210, 0, 255)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
            HapticPattern.WARP_ENGAGE -> {
                // Crescendo swell
                val timings = longArrayOf(0, 40, 30, 60, 30, 100)
                val amplitudes = intArrayOf(0, 80, 0, 160, 0, 255)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
        }
    }

    private fun getLegacyPattern(pattern: HapticPattern): LongArray {
        return when (pattern) {
            HapticPattern.UI_TAP -> longArrayOf(0, 12)
            HapticPattern.LIGHT_SHOT -> longArrayOf(0, 25)
            HapticPattern.KINETIC_IMPACT -> longArrayOf(0, 35, 30, 65)
            HapticPattern.EXPLOSION_MEDIUM -> longArrayOf(0, 50, 30, 90, 40, 60)
            HapticPattern.EXPLOSION_HEAVY -> longArrayOf(0, 90, 40, 140, 50, 180, 60, 120)
            HapticPattern.SHIELD_COLLAPSE -> longArrayOf(0, 30, 20, 40, 20, 60)
            HapticPattern.ORDNANCE_LAUNCH -> longArrayOf(0, 95)
            HapticPattern.CRITICAL_ALARM -> longArrayOf(0, 60, 70, 70)
            HapticPattern.WARP_ENGAGE -> longArrayOf(0, 40, 30, 60, 30, 100)
        }
    }
}
