package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.*
import kotlin.random.Random

enum class SfxType {
    LASER_FIRE,
    PLASMA_BLAST,
    TORPEDO_LAUNCH,
    SHIELD_HIT,
    EXPLOSION_SMALL,
    EXPLOSION_BOSS,
    EMP_PULSE,
    HYPERSPACE_JUMP,
    CRITICAL_ALARM
}

class AudioEngine {
    private val sampleRate = 44100
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(4096)

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    var isMusicEnabled: Boolean = true
    var isSfxEnabled: Boolean = true
    var masterVolume: Float = 0.8f

    @Volatile
    var isPaused: Boolean = false

    // 0.0 = calm orbital exploration, 1.0 = peak intense boss battle
    var combatIntensity: Float = 0f
    var isBossBattle: Boolean = false

    fun resetIntensity() {
        combatIntensity = 0f
        isBossBattle = false
    }

    private val sfxQueue = ConcurrentLinkedQueue<ActiveSfx>()

    private class ActiveSfx(
        val type: SfxType,
        var currentSample: Int = 0,
        val totalSamples: Int
    )

    fun start() {
        if (playbackJob != null) return

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()

            playbackJob = scope.launch {
                val shortBuffer = ShortArray(1024)
                var phaseDrone = 0.0
                var phaseSub = 0.0
                var phasePad = 0.0
                var lfoPhase = 0.0
                var arpIndex = 0
                var arpSampleCounter = 0
                var globalSample: Long = 0

                // Cinematic chord frequencies for space soundtrack:
                // Exploration: Am9 / Fmaj7 (A, C, E, G, B)
                // Combat: Dm / Bb / C / A (D2, F2, A2, C3)
                // Boss Battle: Dark Phrygian / Diminished (C, C#, E, G)
                val calmArp = doubleArrayOf(110.0, 130.81, 164.81, 196.0, 220.0, 261.63)
                val combatArp = doubleArrayOf(73.42, 87.31, 110.0, 130.81, 146.83, 174.61)
                val bossArp = doubleArrayOf(65.41, 69.30, 82.41, 98.0, 110.0, 130.81)

                while (isActive) {
                    if (isPaused) {
                        java.util.Arrays.fill(shortBuffer, 0.toShort())
                        audioTrack?.write(shortBuffer, 0, shortBuffer.size)
                        kotlinx.coroutines.delay(50)
                        continue
                    }

                    val intensity = combatIntensity.coerceIn(0f, 1f)
                    val bpm = if (isBossBattle) 140.0 else (90.0 + intensity * 45.0)
                    val samplesPer16th = (sampleRate * 60.0 / (bpm * 4.0)).toInt()

                    for (i in shortBuffer.indices) {
                        var sampleMix = 0.0

                        // 1. DYNAMIC MUSIC SYNTHESIZER
                        if (isMusicEnabled) {
                            // Sub-bass drone (55Hz / 41Hz)
                            val baseFreq = if (isBossBattle) 43.65 else 55.0
                            phaseDrone += 2.0 * Math.PI * baseFreq / sampleRate
                            phaseSub += 2.0 * Math.PI * (baseFreq * 0.5) / sampleRate
                            lfoPhase += 2.0 * Math.PI * 0.2 / sampleRate // 0.2 Hz slow breath

                            val lfo = (sin(lfoPhase) * 0.5 + 0.5).toFloat()
                            val drone = (sin(phaseDrone) * 0.35 + sin(phaseSub) * 0.45) * (0.4f + lfo * 0.2f)

                            // Synth Pad / String layer (Ethereal space shimmer)
                            val padFreq = baseFreq * 2.0
                            phasePad += 2.0 * Math.PI * padFreq / sampleRate
                            val pad = sin(phasePad) * 0.15 * (1.0 - intensity * 0.4)

                            // Dynamic Combat Arpeggiator / Rhythm
                            arpSampleCounter++
                            if (arpSampleCounter >= samplesPer16th) {
                                arpSampleCounter = 0
                                arpIndex = (arpIndex + 1) % 6
                            }

                            val currentNotes = when {
                                isBossBattle -> bossArp
                                intensity > 0.35f -> combatArp
                                else -> calmArp
                            }
                            val noteFreq = currentNotes[arpIndex]
                            val notePhase = 2.0 * Math.PI * noteFreq * (arpSampleCounter.toDouble() / sampleRate)

                            // Snappy envelope for combat arpeggio notes
                            val noteEnv = exp(-4.5 * (arpSampleCounter.toDouble() / samplesPer16th))
                            // Waveform: pulse wave with filtered harmonic
                            val arpWave = (if (sin(notePhase) > 0.0) 0.8 else -0.8) + sin(notePhase * 2.0) * 0.3
                            val arpMix = arpWave * noteEnv * (0.08 + intensity * 0.45)

                            // Low-frequency kick pulse on beat during combat
                            val isBeat = (arpIndex % 4 == 0)
                            val kickPulse = if (isBeat && intensity > 0.2f) {
                                val kickPhase = 2.0 * Math.PI * 55.0 * (arpSampleCounter.toDouble() / samplesPer16th)
                                sin(kickPhase) * exp(-8.0 * (arpSampleCounter.toDouble() / samplesPer16th)) * (intensity * 0.5)
                            } else 0.0

                            sampleMix += (drone + pad + arpMix + kickPulse) * 0.5
                        }

                        // 2. PROCEDURAL SFX GENERATION
                        if (isSfxEnabled && !sfxQueue.isEmpty()) {
                            val iterator = sfxQueue.iterator()
                            while (iterator.hasNext()) {
                                val sfx = iterator.next()
                                val progress = sfx.currentSample.toFloat() / sfx.totalSamples
                                val t = sfx.currentSample.toDouble() / sampleRate

                                val sfxVal = when (sfx.type) {
                                    SfxType.LASER_FIRE -> {
                                        // Pitch sweeps rapidly from 900Hz down to 180Hz
                                        val freq = 900.0 * (1.0 - progress * 0.8)
                                        val env = (1.0 - progress) * (1.0 - progress)
                                        (if (sin(2.0 * Math.PI * freq * t) > 0) 0.5 else -0.5) * env * 0.6
                                    }
                                    SfxType.PLASMA_BLAST -> {
                                        val freq = 420.0 * exp(-3.0 * progress)
                                        val env = (1.0 - progress)
                                        sin(2.0 * Math.PI * freq * t) * env * 0.7
                                    }
                                    SfxType.TORPEDO_LAUNCH -> {
                                        val freq = 120.0 + progress * 240.0
                                        val noise = (Random.nextDouble() - 0.5) * 0.4
                                        val env = sin(progress * Math.PI.toFloat())
                                        (sin(2.0 * Math.PI * freq * t) * 0.6 + noise) * env * 0.8
                                    }
                                    SfxType.SHIELD_HIT -> {
                                        val freq1 = 800.0 * (1.0 - progress * 0.5)
                                        val freq2 = 1200.0 * (1.0 - progress * 0.5)
                                        val env = exp(-8.0 * progress)
                                        (sin(2.0 * Math.PI * freq1 * t) * 0.5 + sin(2.0 * Math.PI * freq2 * t) * 0.5) * env * 0.7
                                    }
                                    SfxType.EXPLOSION_SMALL -> {
                                        val noise = (Random.nextDouble() - 0.5) * 1.6
                                        val lowRumble = sin(2.0 * Math.PI * 65.0 * t) * 0.8
                                        val env = exp(-3.5 * progress)
                                        (noise + lowRumble) * env * 0.85
                                    }
                                    SfxType.EXPLOSION_BOSS -> {
                                        val noise = (Random.nextDouble() - 0.5) * 2.2
                                        val subRumble = sin(2.0 * Math.PI * 38.0 * t) * 1.2
                                        val env = exp(-1.8 * progress)
                                        (noise + subRumble) * env * 1.1
                                    }
                                    SfxType.EMP_PULSE -> {
                                        val sweep = 1800.0 * (1.0 - progress)
                                        val env = (1.0 - progress)
                                        sin(2.0 * Math.PI * sweep * t) * env * 0.6
                                    }
                                    SfxType.HYPERSPACE_JUMP -> {
                                        val sweep = 100.0 + (progress * progress) * 2200.0
                                        val env = progress
                                        sin(2.0 * Math.PI * sweep * t) * env * 0.8
                                    }
                                    SfxType.CRITICAL_ALARM -> {
                                        val freq = if ((sfx.currentSample / 3500) % 2 == 0) 880.0 else 660.0
                                        sin(2.0 * Math.PI * freq * t) * 0.5
                                    }
                                }

                                sampleMix += sfxVal
                                sfx.currentSample++
                                if (sfx.currentSample >= sfx.totalSamples) {
                                    iterator.remove()
                                }
                            }
                        }

                        // Apply master volume and soft-knee clipping
                        val finalSample = (sampleMix * masterVolume).coerceIn(-1.0, 1.0)
                        shortBuffer[i] = (finalSample * 32767.0).toInt().toShort()
                        globalSample++
                    }

                    audioTrack?.write(shortBuffer, 0, shortBuffer.size)
                }
            }
        } catch (_: Exception) {
            // Audio initialization fallback
        }
    }

    fun playSfx(type: SfxType) {
        if (!isSfxEnabled) return
        val durationSamples = when (type) {
            SfxType.LASER_FIRE -> (sampleRate * 0.18).toInt()
            SfxType.PLASMA_BLAST -> (sampleRate * 0.28).toInt()
            SfxType.TORPEDO_LAUNCH -> (sampleRate * 0.55).toInt()
            SfxType.SHIELD_HIT -> (sampleRate * 0.22).toInt()
            SfxType.EXPLOSION_SMALL -> (sampleRate * 0.65).toInt()
            SfxType.EXPLOSION_BOSS -> (sampleRate * 1.40).toInt()
            SfxType.EMP_PULSE -> (sampleRate * 0.45).toInt()
            SfxType.HYPERSPACE_JUMP -> (sampleRate * 1.10).toInt()
            SfxType.CRITICAL_ALARM -> (sampleRate * 0.80).toInt()
        }

        // Limit concurrent SFX to prevent excessive mixing
        if (sfxQueue.size < 12) {
            sfxQueue.add(ActiveSfx(type, 0, durationSamples))
        }
    }

    fun stop() {
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        sfxQueue.clear()
    }
}
