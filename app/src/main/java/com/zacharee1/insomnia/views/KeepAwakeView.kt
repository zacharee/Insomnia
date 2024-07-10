package com.zacharee1.insomnia.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class KeepAwakeView(context: Context) : View(context) {
    @Suppress("DEPRECATION")
    val params = @SuppressLint("RtlHardcoded")
    object : WindowManager.LayoutParams() {
        init {
            height = 1
            width = 1
            y = -1
            x = -1
            gravity = Gravity.LEFT or Gravity.BOTTOM
            type = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) TYPE_APPLICATION_OVERLAY
                    else TYPE_PRIORITY_PHONE
            flags = FLAG_NOT_FOCUSABLE or
                    FLAG_NOT_TOUCHABLE or
                    FLAG_KEEP_SCREEN_ON or
                    FLAG_LAYOUT_INSET_DECOR or
                    FLAG_LAYOUT_IN_SCREEN or
                    FLAG_LAYOUT_IN_OVERSCAN or
                    FLAG_LAYOUT_NO_LIMITS
        }
    }

    init {
        setBackgroundColor(Color.TRANSPARENT)
    }
}