package com.zacharee1.insomnia.views

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class KeepAwakeView(context: Context) : View(context) {
    val params = object : WindowManager.LayoutParams() {
        init {
            height = 1
            width = 1
            y = 0
            gravity = Gravity.LEFT or Gravity.BOTTOM
            type = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    else WindowManager.LayoutParams.TYPE_PRIORITY_PHONE
            flags = WindowManager.LayoutParams.FLAG_SLIPPERY or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        }
    }

    init {
        setBackgroundColor(Color.YELLOW)
    }
}