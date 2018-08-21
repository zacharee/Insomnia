package com.zacharee1.insomnia.util

import android.os.Parcel
import android.os.Parcelable

class WakeState(val label: Int, val icon: Int, var time: Long) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readLong())

    override fun equals(other: Any?): Boolean {
        return (other is WakeState && other.time == time)
    }

    override fun hashCode(): Int {
        return time.hashCode()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(label)
        parcel.writeInt(icon)
        parcel.writeLong(time)
    }

    override fun describeContents(): Int {
        return time.hashCode()
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