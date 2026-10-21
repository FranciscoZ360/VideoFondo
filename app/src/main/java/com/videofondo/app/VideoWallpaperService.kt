package com.videofondo.app

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaPlayer
import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

/**
 * Fondo de pantalla animado que reproduce en bucle el video elegido por el usuario.
 * Se pausa cuando el fondo no está visible para ahorrar batería.
 */
class VideoWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = VideoEngine()

    inner class VideoEngine : Engine(), SharedPreferences.OnSharedPreferenceChangeListener {

        private var player: MediaPlayer? = null
        private var prepared = false
        private var surfaceReady = false
        private val prefs: SharedPreferences by lazy {
            getSharedPreferences(Prefs.NAME, Context.MODE_PRIVATE)
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            prefs.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onDestroy() {
            prefs.unregisterOnSharedPreferenceChangeListener(this)
            releasePlayer()
            super.onDestroy()
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            surfaceReady = true
            startPlayer(holder)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            surfaceReady = false
            releasePlayer()
            super.onSurfaceDestroyed(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            val p = player ?: return
            if (!prepared) return
            try {
                if (visible) p.start() else p.pause()
            } catch (_: IllegalStateException) {
            }
        }

        override fun onSharedPreferenceChanged(sp: SharedPreferences?, key: String?) {
            if (!surfaceReady) return
            if (key == Prefs.KEY_SOUND && prepared) {
                applyVolume(player)
                return
            }
            releasePlayer()
            startPlayer(surfaceHolder)
        }

        private fun startPlayer(holder: SurfaceHolder) {
            val uriString = prefs.getString(Prefs.KEY_URI, null)
            if (uriString == null) {
                drawMessage(holder, "Abre la app VideoFondo y elige un video")
                return
            }
            try {
                val mp = MediaPlayer()
                player = mp
                prepared = false
                mp.setSurface(holder.surface)
                mp.setDataSource(applicationContext, Uri.parse(uriString))
                mp.isLooping = true
                applyVolume(mp)
                mp.setOnPreparedListener {
                    prepared = true
                    it.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                    if (isVisible) it.start()
                }
                mp.setOnErrorListener { _, _, _ ->
                    releasePlayer()
                    drawMessage(holder, "No se pudo reproducir el video")
                    true
                }
                mp.prepareAsync()
            } catch (e: Exception) {
                releasePlayer()
                drawMessage(holder, "No se pudo abrir el video. Elígelo de nuevo en la app.")
            }
        }

        private fun applyVolume(mp: MediaPlayer?) {
            val v = if (prefs.getBoolean(Prefs.KEY_SOUND, false)) 1f else 0f
            try {
                mp?.setVolume(v, v)
            } catch (_: IllegalStateException) {
            }
        }

        private fun releasePlayer() {
            prepared = false
            player?.let {
                try {
                    it.reset()
                } catch (_: Exception) {
                }
                it.release()
            }
            player = null
        }

        private fun drawMessage(holder: SurfaceHolder, text: String) {
            val canvas = try {
                holder.lockCanvas()
            } catch (_: Exception) {
                null
            } ?: return
            try {
                canvas.drawColor(Color.BLACK)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = canvas.width / 22f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(text, canvas.width / 2f, canvas.height / 2f, paint)
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }
}
