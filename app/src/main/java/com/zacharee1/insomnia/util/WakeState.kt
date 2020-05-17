package com.zacharee1.insomnia.util

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Parcel
import android.os.Parcelable
import com.zacharee1.insomnia.R
import java.util.*

class WakeState(var time: Long) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong()
    )

    override fun equals(other: Any?): Boolean {
        return (other is WakeState && other.time == time)
    }

    override fun hashCode(): Int {
        return time.hashCode()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(time)
    }

    override fun describeContents(): Int {
        return time.hashCode()
    }

    fun createLabelFromTime(context: Context): String {
        val format = SimpleDateFormat("mm:ss", Locale.getDefault())
        return when {
            time < 0 -> context.resources.getString(R.string.time_infinite)
            time == 0L -> context.resources.getString(R.string.app_name)
            else -> format.format(Date(time))
        }
    }

    fun drawableResourceFromTime(): Int {
        return if (time == 0L) R.drawable.off else R.drawable.on
    }

    companion object CREATOR : Parcelable.Creator<WakeState> {
        override fun createFromParcel(parcel: Parcel): WakeState {
            return WakeState(parcel)
        }

        override fun newArray(size: Int): Array<WakeState?> {
            return arrayOfNulls(size)
        }
    }
}