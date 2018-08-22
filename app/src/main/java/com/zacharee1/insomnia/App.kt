package com.zacharee1.insomnia

import android.app.Application
import android.content.*
import android.net.Uri
import android.os.BatteryManager
import android.os.CountDownTimer
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.content.LocalBroadcastManager
import android.view.WindowManager
import android.widget.Toast
import com.zacharee1.insomnia.tiles.CycleTile
import com.zacharee1.insomnia.util.*
import com.zacharee1.insomnia.views.KeepAwakeView

class App : Application(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        const val ACTION_UPDATE = "com.zacharee1.insomnia.action.UPDATE"

        const val TIME_OFF = 0

        const val ZERO_MIN = 0L
        const val ONE_MIN = 60 * 1000L
        const val FIVE_MIN = 300 * 1000L
        const val TEN_MIN = 600 * 1000L
        const val THIRTY_MIN = 1800 * 1000L
        const val INFINITE_MIN = -1L

        val STATE_OFF = WakeState(R.string.app_name, R.drawable.off, ZERO_MIN)
        val STATE_INFINITE = WakeState(R.string.time_infinite, R.drawable.on, INFINITE_MIN)

        val DEFAULT_STATES = arrayListOf(
                WakeState(R.string.time_1, R.drawable.on, ONE_MIN),
                WakeState(R.string.time_5, R.drawable.on, FIVE_MIN),
                WakeState(R.string.time_10, R.drawable.on, TEN_MIN),
                WakeState(R.string.time_30, R.drawable.on, THIRTY_MIN),
                STATE_INFINITE
        )

        fun get(context: Context): App {
            return context.applicationContext as App
        }
    }

    val wm by lazy { getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    val view by lazy { KeepAwakeView(this) }
    val states = ArrayList<WakeState>()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_BATTERY_CHANGED -> {
                    if (activateWhenPlugged()) {
                        val state = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                        val plugged = state == BatteryManager.BATTERY_PLUGGED_AC
                                || state == BatteryManager.BATTERY_PLUGGED_USB
                                || state == BatteryManager.BATTERY_PLUGGED_WIRELESS

                        if (plugged) setToState(STATE_INFINITE)
                        else setToState(STATE_OFF)
                    }
                }

                Intent.ACTION_SCREEN_OFF -> disable()
            }
        }
    }
    private val filter = IntentFilter().apply {
        addAction(Intent.ACTION_BATTERY_CHANGED)
        addAction(Intent.ACTION_SCREEN_OFF)
    }

    var isEnabled = false

    private var timer: CountDownTimer? = null
    var currentTime = TIME_OFF
    var currentState = STATE_OFF
    var timerRunning = false

    override fun onCreate() {
        super.onCreate()

        populateStates()
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this)
        registerReceiver(receiver, filter)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            KEY_STATES -> populateStates()
        }
    }

    fun enable(): Boolean {
        return if (Settings.canDrawOverlays(this)) {
            try {
                wm.removeView(view)
            } catch (e: Exception) {}

            try {
                wm.addView(view, view.params)
            } catch (e: Exception) {
                e.localizedMessage.loge()
            }

            isEnabled = true
            broadcastUpdate()
            true
        } else {
            launchOverlaySettings()
            broadcastUpdate()
            false
        }
    }

    fun disable() {
        try {
            wm.removeView(view)
        } catch (e: Exception) {}

        isEnabled = false
        currentState = STATE_OFF
        currentTime = TIME_OFF

        stopCountDown()
        broadcastUpdate()
    }

    fun cycle() {
        var newIndex = currentTime + 1
        if (newIndex >= states.size) newIndex = 0

        setToState(newIndex)
    }

    fun setToState(time: Int) {
        setToState(states[time])
    }

    fun setToState(state: WakeState?) {
        val newState = state ?: STATE_OFF
        disable()
        currentTime = getStateIndexByTime(newState.time)
        currentState = newState

        when (newState.time) {
            ZERO_MIN -> {
                disable()
                timerRunning = false
            }

            INFINITE_MIN -> {
                if (enable()) {
                    timerRunning = true
                } else {
                    disable()
                }
            }

            else -> {
                makeCountDown(newState.time)
            }
        }
    }

    fun getStateIndexByTime(time: Long): Int {
        val filtered = states.filter { it.time == time }
        if (states.isEmpty() || filtered.isEmpty()) return 0
        val index = states.indexOf(filtered[0])
        return if (index == -1) 0 else index
    }

    fun broadcastUpdate() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_UPDATE))
    }

    private fun populateStates() {
        disable()

        states.clear()
        states.add(STATE_OFF)

        states.addAll(getSavedTimes())
    }

    private fun stopCountDown() {
        timer?.cancel()
        timerRunning = false
    }

    private fun makeCountDown(timeMillis: Long) {
        if (enable()) {
            timer = object : CountDownTimer(timeMillis, 1000) {
                override fun onFinish() {
                    timerRunning = false
                    disable()
                }

                override fun onTick(millisUntilFinished: Long) {
                    CycleTile.tick(this@App, millisUntilFinished)
                }
            }.start()

            timerRunning = true
        } else {
            disable()
        }
    }

    private fun launchOverlaySettings() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

        Toast.makeText(this, R.string.enable_overlay, Toast.LENGTH_SHORT).show()
    }
}