package com.zacharee1.insomnia.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.zacharee1.insomnia.App
import com.zacharee1.insomnia.R

const val TAG = "Insomnia"

const val KEY_STATES = "states"
const val TURN_ON_WHEN_PLUGGED_IN = "turn_on_plugged"
const val DELIMITER = "/"

fun String.loge() {
    Log.e(TAG, this)
}

fun Context.getSavedTimes(): ArrayList<WakeState> {
    val gson = Gson()
    val strings = PreferenceManager.getDefaultSharedPreferences(this).getString(KEY_STATES, null) ?: return App.DEFAULT_STATES
    val ret = ArrayList<WakeState>()

    strings.split(DELIMITER).forEach {
        val state = gson.fromJson(it, WakeState::class.java)
        if (state != null) ret.add(state)
    }

    return ret
}

fun Context.saveTimes(times: List<WakeState>) {
    val gson = Gson()
    val strings = ArrayList<String>()

    times.forEach {
        strings.add(gson.toJson(it))
    }

    PreferenceManager.getDefaultSharedPreferences(this).edit().putString(KEY_STATES, TextUtils.join(DELIMITER, strings)).apply()
}

fun Context.launchOverlaySettings(launcher: Intent.() -> Unit) {
    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.launcher()

    Toast.makeText(this, R.string.enable_overlay, Toast.LENGTH_SHORT).show()
}

fun Context.activateWhenPlugged() =
        PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(TURN_ON_WHEN_PLUGGED_IN, false)

fun Context.setActivateWhenPlugged(activate: Boolean) =
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(TURN_ON_WHEN_PLUGGED_IN, activate)
                .apply()