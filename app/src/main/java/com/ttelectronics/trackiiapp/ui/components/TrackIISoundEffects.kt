package com.ttelectronics.trackiiapp.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

class RawSoundPlayer(context: Context, rawName: String) {
    private val appContext = context.applicationContext
    private val soundPool: SoundPool
    private val soundId: Int

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setAudioAttributes(attributes)
            .setMaxStreams(2)
            .build()
        val resId = appContext.resources.getIdentifier(rawName, "raw", appContext.packageName)
        soundId = if (resId != 0) soundPool.load(appContext, resId, 1) else 0
    }

    fun play(leftVolume: Float = 1f, rightVolume: Float = 1f, rate: Float = 1f) {
        if (soundId != 0) {
            soundPool.play(soundId, leftVolume, rightVolume, 1, 0, rate)
        }
    }

    fun release() {
        soundPool.release()
    }
}

@Composable
fun rememberRawSoundPlayer(rawName: String): RawSoundPlayer {
    val context = androidx.compose.ui.platform.LocalContext.current
    val player = remember(context, rawName) { RawSoundPlayer(context, rawName) }
    DisposableEffect(player) {
        onDispose { player.release() }
    }
    return player
}
