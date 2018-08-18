package com.zacharee1.insomnia.tiles

import android.content.*
import android.graphics.drawable.Icon
import android.os.CountDownTimer
import android.preference.PreferenceManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.support.v4.content.LocalBroadcastManager
import com.zacharee1.insomnia.App
import com.zacharee1.insomnia.R
import com.zacharee1.insomnia.util.KEY_STATES
import com.zacharee1.insomnia.util.KEY_USE_INFINITE
import com.zacharee1.insomnia.util.getSavedTimes
import com.zacharee1.insomnia.util.useInfinite
import java.util.concurrent.TimeUnit

class CycleTile : TileService(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        const val DELIMITER = "/"
        const val ACTION_SET_TIME = "com.zacharee1.insomnia.action.SET_TIME"

        const val EXTRA_TIME_MS = "time_ms"

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
                WakeState(R.string.time_30, R.drawable.on, THIRTY_MIN)
        )

        fun setTime(context: Context, time: Long) {
            val intent = Intent(ACTION_SET_TIME)
            intent.putExtra(EXTRA_TIME_MS, time)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }

    private val app by lazy { App.get(this) }
    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private val states = ArrayList<WakeState>()
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                ACTION_SET_TIME -> {
                    if (intent.hasExtra(EXTRA_TIME_MS)) setToTime(intent.getLongExtra(EXTRA_TIME_MS, ZERO_MIN))
                }
            }
        }
    }

    private var timer: CountDownTimer? = null
    private var currentTime = TIME_OFF
    private var timerRunning = false

    override fun onCreate() {
        populateStates()
        prefs.registerOnSharedPreferenceChangeListener(this)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(ACTION_SET_TIME))
    }

    override fun onDestroy() {
        prefs.unregisterOnSharedPreferenceChangeListener(this)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            KEY_STATES,
            KEY_USE_INFINITE -> populateStates()
        }
    }

    override fun onStartListening() {
        if (!timerRunning) {
            setOffState()
        }
    }

    override fun onClick() {
        var newIndex = currentTime + 1
        if (newIndex >= states.size) newIndex = 0

        setToState(newIndex)
    }

    fun setToState(time: Int) {
        stopCountDown()

        currentTime = time

        val newState = states[time]

        qsTile?.icon = Icon.createWithResource(this, newState.icon)
        qsTile?.updateTile()

        when (newState.time) {
            ZERO_MIN -> {
                app.disable()
                setOffState()
                timerRunning = false
            }

            INFINITE_MIN -> {
                if (app.enable()) {
                    qsTile?.label = resources.getText(newState.label)
                    setActive()
                    timerRunning = true
                } else {
                    setOffState()
                }
            }

            else -> {
                makeCountDown(newState.time)
            }
        }
    }

    fun setToTime(time: Long) {
        setToState(getStateIndexByTime(time))
    }

    fun getStateIndexByTime(time: Long): Int {
        val filtered = states.filter { it.time == time }
        return states.indexOf(filtered[0])
    }

    private fun setOffState() {
        qsTile?.label = resources.getText(STATE_OFF.label)
        qsTile?.icon = Icon.createWithResource(this, STATE_OFF.icon)
        qsTile?.state = Tile.STATE_INACTIVE
        qsTile?.updateTile()
        currentTime = TIME_OFF
    }

    private fun setActive() {
        qsTile?.state = Tile.STATE_ACTIVE
        qsTile?.updateTile()
    }

    private fun stopCountDown() {
        timer?.cancel()
        timerRunning = false
    }

    private fun makeCountDown(timeMillis: Long) {
        if (app.enable()) {
            setActive()

            timer = object : CountDownTimer(timeMillis, 1000) {
                override fun onFinish() {
                    timerRunning = false
                    app.disable()
                    setOffState()
                }

                override fun onTick(millisUntilFinished: Long) {
                    qsTile?.label = String.format("%d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                    TimeUnit.MINUTES.toSeconds(
                                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
                    )
                    qsTile?.updateTile()
                }
            }.start()

            timerRunning = true
        } else {
            setOffState()
        }
    }

    private fun populateStates() {
        stopCountDown()
        setOffState()

        states.clear()
        states.add(STATE_OFF)

        states.addAll(getSavedTimes())

        if (useInfinite()) states.add(STATE_INFINITE)
    }

    class WakeState(val label: Int, val icon: Int, var time: Long) {
        override fun equals(other: Any?): Boolean {
            return (other is WakeState && other.time == time)
        }

        override fun hashCode(): Int {
            return time.hashCode()
        }
    }
}