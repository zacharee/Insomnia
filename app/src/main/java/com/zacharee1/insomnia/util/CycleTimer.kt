package com.zacharee1.insomnia.util

import android.os.CountDownTimer
import android.os.SystemClock

abstract class CycleTimer(private val millisInFuture: Long, countDownInterval: Long) : CountDownTimer(millisInFuture, countDownInterval) {
    val stopTimeInFuture: Long
        get() = CountDownTimer::class.java
                .getDeclaredField("mStopTimeInFuture")
                .apply { isAccessible = true }
                .getLong(this)

    val startTime: Long
        get() = stopTimeInFuture - millisInFuture

    val remainingTime: Long
        get() = stopTimeInFuture - SystemClock.elapsedRealtime()

    val elapsedTime: Long
        get() = stopTimeInFuture - remainingTime - startTime + 1000
}