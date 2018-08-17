package com.zacharee1.insomnia.util

import android.content.Context
import android.preference.PreferenceManager
import android.text.TextUtils
import com.google.gson.Gson
import com.zacharee1.insomnia.tiles.CycleTile

object Utils {
    const val KEY_USE_INFINITE = "use_infinite"
    const val KEY_STATES = "states"

    fun getSavedTimes(context: Context): ArrayList<CycleTile.WakeState> {
        val gson = Gson()
        val strings = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_STATES, null) ?: return CycleTile.DEFAULT_STATES
        val ret = ArrayList<CycleTile.WakeState>()

        strings.split(CycleTile.DELIMITER).forEach {
            val state = gson.fromJson<CycleTile.WakeState>(it, CycleTile.WakeState::class.java)
            if (state != null) ret.add(state)
        }

        return ret
    }

    fun saveTimes(context: Context, times: ArrayList<CycleTile.WakeState>) {
        val gson = Gson()
        val strings = ArrayList<String>()

        times.forEach {
            strings.add(gson.toJson(it))
        }

        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_STATES, TextUtils.join(CycleTile.DELIMITER, strings)).apply()
    }

    fun useInfinite(context: Context)
        = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_USE_INFINITE, true)

    fun setUseInfinite(context: Context, useInfinite: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_USE_INFINITE, useInfinite).apply()
    }
}