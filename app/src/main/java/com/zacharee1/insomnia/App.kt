package com.zacharee1.insomnia

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import com.zacharee1.insomnia.util.loge
import com.zacharee1.insomnia.views.KeepAwakeView

class App : Application() {
    companion object {
        fun get(context: Context): App {
            return context.applicationContext as App
        }
    }

    val wm by lazy { getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    val view by lazy { KeepAwakeView(this) }

    var isEnabled = false

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> disable()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        registerReceiver(screenStateReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
    }

    fun enable(): Boolean {
        return if (Settings.canDrawOverlays(this)) {
            try {
                wm.removeView(view)
            } catch (e: Exception) {}

            try {
                wm.addView(view, view.params)
            } catch (e: Exception) {
                loge(e.localizedMessage)
            }

            isEnabled = true
            true
        } else {
            launchOverlaySettings()
            false
        }
    }

    fun disable() {
        try {
            wm.removeView(view)
        } catch (e: Exception) {}

        isEnabled = false
    }

    private fun launchOverlaySettings() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

        Toast.makeText(this, R.string.enable_overlay, Toast.LENGTH_SHORT).show()
    }
}