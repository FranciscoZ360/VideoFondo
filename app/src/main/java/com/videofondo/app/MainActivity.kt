package com.videofondo.app

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity(), TextureView.SurfaceTextureListener {

    private val requestPickVideo = 42

    private lateinit var texture: TextureView
    private lateinit var emptyState: View
    private lateinit var clockOverlay: View
    private lateinit var status: TextView
    private lateinit var statusDot: View
    private lateinit var soundIcon: ImageView
    private lateinit var soundHint: TextView

    private var player: MediaPlayer? = null
    private var surface: Surface? = null
    private var resumed = false

    private val prefs by lazy { getSharedPreferences(Prefs.NAME, Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupEdgeToEdge()
        setContentView(R.layout.activity_main)

        texture = findViewById(R.id.preview)
        emptyState = findViewById(R.id.empty_state)
        clockOverlay = findViewById(R.id.clock_overlay)
        status = findViewById(R.id.status)
        statusDot = findViewById(R.id.status_dot)
        soundIcon = findViewById(R.id.sound_icon)
        soundHint = findViewById(R.id.sound_hint)

        // Respeta la barra de estado y la de navegación.
        val root = findViewById<View>(R.id.content)
        val basePad = root.paddingTop
        root.setOnApplyWindowInsetsListener { v, insets ->
            @Suppress("DEPRECATION")
            v.setPadding(
                v.paddingLeft,
                basePad + insets.systemWindowInsetTop,
                v.paddingRight,
                basePad + insets.systemWindowInsetBottom
            )
            insets
        }

        texture.surfaceTextureListener = this

        val sound = findViewById<Switch>(R.id.sound)
        sound.isChecked = prefs.getBoolean(Prefs.KEY_SOUND, false)
        updateSoundUi(sound.isChecked)
        sound.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(Prefs.KEY_SOUND, checked).apply()
            updateSoundUi(checked)
        }
        findViewById<View>(R.id.sound_row).setOnClickListener { sound.toggle() }

        findViewById<View>(R.id.phone).setOnClickListener { pickVideo() }
        findViewById<View>(R.id.pick).setOnClickListener { pickVideo() }
        findViewById<View>(R.id.apply).setOnClickListener { setAsWallpaper() }

        refreshState()
    }

    @Suppress("DEPRECATION")
    private fun setupEdgeToEdge() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    }

    override fun onResume() {
        super.onResume()
        resumed = true
        startPreview()
    }

    override fun onPause() {
        resumed = false
        releasePlayer()
        super.onPause()
    }

    // --- Vista previa del video ---

    override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
        surface = Surface(st)
        startPreview()
    }

    override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) {
        player?.let { applyCenterCrop(it.videoWidth, it.videoHeight) }
    }

    override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
        releasePlayer()
        surface?.release()
        surface = null
        return true
    }

    override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}

    private fun startPreview() {
        releasePlayer()
        val s = surface ?: return
        if (!resumed) return
        val uriString = prefs.getString(Prefs.KEY_URI, null) ?: return
        try {
            val mp = MediaPlayer()
            player = mp
            mp.setSurface(s)
            mp.setDataSource(this, Uri.parse(uriString))
            mp.isLooping = true
            mp.setVolume(0f, 0f)
            mp.setOnVideoSizeChangedListener { _, w, h -> applyCenterCrop(w, h) }
            mp.setOnPreparedListener {
                applyCenterCrop(it.videoWidth, it.videoHeight)
                it.start()
                texture.animate().alpha(1f).setDuration(400).start()
            }
            mp.setOnErrorListener { _, _, _ ->
                setStatus(getString(R.string.video_error), false)
                releasePlayer()
                true
            }
            mp.prepareAsync()
        } catch (e: Exception) {
            setStatus(getString(R.string.video_error), false)
            releasePlayer()
        }
    }

    /** Recorta el video para llenar el marco sin deformarlo, igual que el fondo real. */
    private fun applyCenterCrop(videoW: Int, videoH: Int) {
        val viewW = texture.width.toFloat()
        val viewH = texture.height.toFloat()
        if (videoW <= 0 || videoH <= 0 || viewW <= 0f || viewH <= 0f) return
        val scale = maxOf(viewW / videoW, viewH / videoH)
        val sx = videoW * scale / viewW
        val sy = videoH * scale / viewH
        val m = Matrix()
        m.setScale(sx, sy, viewW / 2f, viewH / 2f)
        texture.setTransform(m)
    }

    private fun releasePlayer() {
        player?.let {
            try {
                it.reset()
            } catch (_: Exception) {
            }
            it.release()
        }
        player = null
    }

    // --- Estado de la interfaz ---

    private fun refreshState() {
        val hasVideo = prefs.getString(Prefs.KEY_URI, null) != null
        emptyState.visibility = if (hasVideo) View.GONE else View.VISIBLE
        clockOverlay.visibility = if (hasVideo) View.VISIBLE else View.GONE
        findViewById<TextView>(R.id.pick_label).setText(
            if (hasVideo) R.string.change else R.string.pick
        )
        if (!hasVideo) texture.alpha = 0f
        setStatus(getString(if (hasVideo) R.string.video_ready else R.string.no_video), hasVideo)
    }

    private fun setStatus(text: String, ok: Boolean) {
        status.text = text
        statusDot.setBackgroundResource(if (ok) R.drawable.dot_ok else R.drawable.dot_idle)
    }

    private fun updateSoundUi(on: Boolean) {
        soundIcon.setImageResource(if (on) R.drawable.ic_volume_on else R.drawable.ic_volume_off)
        soundHint.setText(if (on) R.string.sound_on else R.string.sound_off)
    }

    // --- Acciones ---

    private fun pickVideo() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*"
        }
        startActivityForResult(intent, requestPickVideo)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != requestPickVideo || resultCode != RESULT_OK) return
        val uri = data?.data ?: return

        // Permiso permanente para leer el video aunque se reinicie el teléfono.
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) {
        }

        // Libera el permiso del video anterior, si había uno distinto.
        prefs.getString(Prefs.KEY_URI, null)?.let { old ->
            if (old != uri.toString()) {
                try {
                    contentResolver.releasePersistableUriPermission(
                        Uri.parse(old), Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                }
            }
        }

        prefs.edit().putString(Prefs.KEY_URI, uri.toString()).apply()
        texture.alpha = 0f
        refreshState()
        // onResume se ejecuta después y vuelve a iniciar la vista previa.
    }

    private fun setAsWallpaper() {
        if (prefs.getString(Prefs.KEY_URI, null) == null) {
            Toast.makeText(this, R.string.no_video, Toast.LENGTH_SHORT).show()
            pickVideo()
            return
        }
        val component = ComponentName(this, VideoWallpaperService::class.java)
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
        try {
            startActivity(intent)
        } catch (_: Exception) {
            try {
                startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
            } catch (_: Exception) {
                Toast.makeText(this, R.string.no_picker, Toast.LENGTH_LONG).show()
            }
        }
    }
}
