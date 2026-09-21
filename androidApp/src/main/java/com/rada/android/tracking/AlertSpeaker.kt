package com.rada.android.tracking

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import com.rada.shared.domain.ReportType
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

class AlertSpeaker(context: Context) {

    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(AudioManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
    private var focusRequest: AudioFocusRequest? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pendingSpeech: String? = null

    init {
        tts = TextToSpeech(appContext) { status ->
            val engine = tts ?: return@TextToSpeech
            if (status != TextToSpeech.SUCCESS) return@TextToSpeech
            val kenya = engine.setLanguage(Locale("en", "KE"))
            if (kenya == TextToSpeech.LANG_MISSING_DATA || kenya == TextToSpeech.LANG_NOT_SUPPORTED) {
                engine.setLanguage(Locale.UK)
            }
            engine.setAudioAttributes(attributes)
            ttsReady = true
            pendingSpeech?.let { speak(it) }
            pendingSpeech = null
        }
    }

    fun warn(type: ReportType, distanceMeters: Int, placeName: String?) {
        val what = when (type) {
            ReportType.CAMERA -> "Speed camera"
            ReportType.CHECKPOINT -> "Police checkpoint"
            ReportType.ALCOBLOW -> "Alcohol check"
        }
        val rounded = if (distanceMeters >= 100) (distanceMeters / 50) * 50 else (distanceMeters / 10) * 10
        val where = placeName?.let { ", $it" } ?: ""
        acquireFocus()
        play(CHIME_TWO_TONE)
        handler.postDelayed({ speak("$what ahead$where. $rounded metres.") }, CHIME_TWO_TONE_MS + 150)
    }

    fun finalWarning() {
        acquireFocus()
        play(CHIME_TRIPLE)
    }

    fun release() {
        handler.removeCallbacksAndMessages(null)
        tts?.stop()
        tts?.shutdown()
        tts = null
        abandonFocus()
    }

    private fun speak(text: String) {
        val engine = tts
        if (engine == null || !ttsReady) {
            pendingSpeech = text
            return
        }
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "rada-alert")
        handler.postDelayed({ abandonFocus() }, SPEECH_FOCUS_HOLD_MS)
    }

    private fun play(samples: ShortArray) {
        val track = AudioTrack.Builder()
            .setAudioAttributes(attributes)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(samples, 0, samples.size)
        track.play()
        handler.postDelayed({ runCatching { track.release() } }, samples.size * 1000L / SAMPLE_RATE + 300)
    }

    private fun acquireFocus() {
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(attributes)
            .build()
        focusRequest = request
        audioManager.requestAudioFocus(request)
    }

    private fun abandonFocus() {
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        focusRequest = null
    }

    private companion object {
        const val SAMPLE_RATE = 22_050
        const val SPEECH_FOCUS_HOLD_MS = 6_000L

        fun tone(frequency: Double, millis: Int): ShortArray {
            val count = SAMPLE_RATE * millis / 1000
            return ShortArray(count) { i ->
                val t = i.toDouble() / SAMPLE_RATE
                val fade = minOf(1.0, i / 200.0, (count - i) / 400.0)
                (sin(2 * PI * frequency * t) * fade * Short.MAX_VALUE * 0.95).toInt().toShort()
            }
        }

        fun silence(millis: Int) = ShortArray(SAMPLE_RATE * millis / 1000)

        val CHIME_TWO_TONE = tone(880.0, 260) + silence(60) + tone(1175.0, 340)
        const val CHIME_TWO_TONE_MS = 660L
        val CHIME_TRIPLE = tone(1320.0, 140) + silence(70) + tone(1320.0, 140) + silence(70) + tone(1320.0, 220)
    }
}
