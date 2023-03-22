package com.zacharee1.insomnia

import android.app.Application
import android.content.*
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.zacharee1.insomnia.util.*
import com.zacharee1.insomnia.views.KeepAwakeView
import org.lsposed.hiddenapibypass.HiddenApiBypass

class App : Application(), SharedPreferences.OnSharedPreferenceChangeListener, EventListener {
    companion object {
        const val TIME_OFF = 0

        const val ZERO_MIN = 0L
        const val ONE_MIN = 60 * 1000L
        const val FIVE_MIN = 300 * 1000L
        const val TEN_MIN = 600 * 1000L
        const val THIRTY_MIN = 1800 * 1000L
        const val INFINITE_MIN = -1L

        val STATE_OFF by lazy { WakeState(ZERO_MIN) }
        val STATE_INFINITE = WakeState(INFINITE_MIN)

        val DEFAULT_STATES = arrayListOf(
                WakeState(ONE_MIN),
                WakeState(FIVE_MIN),
                WakeState(TEN_MIN),
                WakeState(THIRTY_MIN),
                STATE_INFINITE
        )

        fun get(context: Context): App {
            return context.applicationContext as App
        }
    }

    private val wm by lazy { getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private val view by lazy { KeepAwakeView(this) }
    private val states = ArrayList<WakeState>()

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

    private var isEnabled = false

    private var timer: CycleTimer? = null
    private var currentTime = TIME_OFF
    private var currentState = STATE_OFF
    var timerRunning = false

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.setHiddenApiExemptions("")
        }

        eventManager.addListener(this)

        populateStates()
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this)
        registerReceiver(receiver, filter)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            KEY_STATES -> populateStates()
        }
    }

    private fun enable(): Boolean {
        return if (Settings.canDrawOverlays(this)) {
            try {
                wm.removeView(view)
            } catch (_: Exception) {}

            try {
                wm.addView(view, view.params)
            } catch (e: Exception) {
                e.localizedMessage?.loge()
            }

            isEnabled = true
            sendUpdate()
            true
        } else {
            launchOverlaySettings()
            sendUpdate()
            false
        }
    }

    fun disable() {
        try {
            wm.removeView(view)
        } catch (_: Exception) {}

        isEnabled = false
        currentState = STATE_OFF
        currentTime = TIME_OFF

        stopCountDown()
        sendUpdate()
    }

    private fun cycle() {
        if (timerRunning && (timer?.elapsedTime ?: 0) > 10000L) {
            setToState(STATE_OFF)
        } else {
            var newIndex = currentTime + 1
            if (newIndex >= states.size) newIndex = 0

            setToState(newIndex)
        }
    }

    private fun setToState(time: Int) {
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

    override fun onEvent(event: Event) {
        when (event) {
            Event.Cycle -> cycle()
            Event.RequestUpdate -> sendUpdate()
            else -> {}
        }
    }

    private fun getStateIndexByTime(time: Long): Int {
        val filtered = states.filter { it.time == time }
        if (states.isEmpty() || filtered.isEmpty()) return 0
        val index = states.indexOf(filtered[0])
        return if (index == -1) 0 else index
    }

    private fun sendUpdate() {
        eventManager.sendEvent(Event.NewState(currentState))
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
            timer = object : CycleTimer(timeMillis, 1000) {
                override fun onFinish() {
                    timerRunning = false
                    disable()
                }

                override fun onTick(millisUntilFinished: Long) {
                    eventManager.sendEvent(Event.Tick(millisUntilFinished))
                }
            }
            timer?.start()

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