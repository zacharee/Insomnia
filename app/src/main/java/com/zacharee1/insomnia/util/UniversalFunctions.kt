package com.zacharee1.insomnia.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import com.google.gson.Gson
import com.zacharee1.insomnia.tiles.CycleTile

const val TAG = "Insomnia"

const val KEY_USE_INFINITE = "use_infinite"
const val KEY_STATES = "states"

fun loge(message: String) {
    Log.e(TAG, message)
}

fun logv(message: String) {
    Log.v(TAG, message)
}

fun logi(message: String) {
    Log.i(TAG, message)
}

fun logd(message: String) {
    Log.d(TAG, message)
}

fun logw(message: String) {
    Log.w(TAG, message)
}

fun loga(message: String) {
    Log.println(Log.ASSERT, TAG, message)
}

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

/**
 * Convert a certain DP value to its equivalent in px
 * @param context context object
 * @param dpVal the chosen DP value
 * @return the DP value in terms of px
 */
fun dpAsPx(context: Context, dpVal: Int) =
        dpAsPx(context, dpVal.toFloat())

fun dpAsPx(context: Context, dpVal: Float) =
        Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, context.resources.displayMetrics))

fun drawableToBitmap(drawable: Drawable): Bitmap {

    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }

    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}