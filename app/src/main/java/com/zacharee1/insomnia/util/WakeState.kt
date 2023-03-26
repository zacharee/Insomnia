package com.zacharee1.insomnia.util

import android.content.Context
import android.os.Parcelable
import com.zacharee1.insomnia.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class WakeState(var time: Long) : Parcelable {
    fun createLabelFromTime(context: Context): String {
        val hours = time / 1000 / 60 / 60
        val minutes = time / 1000 / 60 % 60
        val seconds = time / 1000 % 60

        return when {
            time < 0 -> context.resources.getString(R.string.time_infinite)
            time == 0L -> context.resources.getString(R.string.app_name)
            else -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

    fun drawableResourceFromTime(): Int {
        return if (time == 0L) R.drawable.off else R.drawable.on
    }
}