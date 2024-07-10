package com.zacharee1.insomnia.util

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.zacharee1.insomnia.App

const val TAG = "Insomnia"

const val KEY_STATES = "states"
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

fun Context.activateWhenPlugged() =
        PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("turn_on_plugged", false)

fun Context.setActivateWhenPlugged(activate: Boolean) =
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean("turn_on_plugged", activate)
                .apply()