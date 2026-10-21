package com.videofondo.app

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView

class MainActivity : Activity() {

    private val requestPickVideo = 42

    private lateinit var preview: VideoView
    private lateinit var status: TextView

    private val prefs by lazy { getSharedPreferences(Prefs.NAME, Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preview = findViewById(R.id.preview)
        status = findViewById(R.id.status)
        val sound = findViewById<Switch>(R.id.sound)

        sound.isChecked = prefs.getBoolean(Prefs.KEY_SOUND, false)
        sound.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(Prefs.KEY_SOUND, checked).apply()
        }

        findViewById<Button>(R.id.pick).setOnClickListener { pickVideo() }
        findViewById<Button>(R.id.apply).setOnClickListener { setAsWallpaper() }

        showPreview()
    }

    override fun onResume() {
        super.onResume()
        if (::preview.isInitialized) preview.start()
    }

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
        showPreview()
    }

    private fun showPreview() {
        val uriString = prefs.getString(Prefs.KEY_URI, null)
        if (uriString == null) {
            status.text = getString(R.string.no_video)
            return
        }
        status.text = getString(R.string.video_ready)
        preview.setVideoURI(Uri.parse(uriString))
        preview.setOnPreparedListener { mp ->
            mp.isLooping = true
            mp.setVolume(0f, 0f)
            preview.start()
        }
        preview.setOnErrorListener { _, _, _ ->
            status.text = getString(R.string.video_error)
            true
        }
    }

    private fun setAsWallpaper() {
        if (prefs.getString(Prefs.KEY_URI, null) == null) {
            Toast.makeText(this, R.string.no_video, Toast.LENGTH_SHORT).show()
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
