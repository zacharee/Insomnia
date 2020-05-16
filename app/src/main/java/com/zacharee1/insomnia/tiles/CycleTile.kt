package com.zacharee1.insomnia.tiles

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.preference.PreferenceManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.zacharee1.insomnia.App
import com.zacharee1.insomnia.App.Companion.STATE_OFF
import com.zacharee1.insomnia.App.Companion.ZERO_MIN
import com.zacharee1.insomnia.util.WakeState
import java.util.concurrent.TimeUnit

class CycleTile : TileService() {
    companion object {
        const val ACTION_SET_STATE = "com.zacharee1.insomnia.action.SET_STATE"
        const val ACTION_TICK = "com.zacharee1.insomnia.action.TICK"

        const val EXTRA_STATE = "state"
        const val EXTRA_TIME = "time"

        fun setState(context: Context, state: WakeState) {
            val intent = Intent(ACTION_SET_STATE)
            intent.putExtra(EXTRA_STATE, state)
            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }

        fun tick(context: Context, time: Long) {
            val intent = Intent(ACTION_TICK)
            intent.putExtra(EXTRA_TIME, time)
            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }

    private val app by lazy { App.get(this) }
    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_SET_STATE -> {
                    if (intent.hasExtra(EXTRA_STATE)) setToState(intent.getParcelableExtra(EXTRA_STATE))
                }

                ACTION_TICK -> {
                    if (intent.hasExtra(EXTRA_TIME)) onTick(intent.getLongExtra(EXTRA_TIME, ZERO_MIN))
                }

                App.ACTION_UPDATE -> {
                    setToState(app.currentState)
                }
            }
        }
    }

    override fun onCreate() {
        val filter = IntentFilter()
        filter.addAction(ACTION_SET_STATE)
        filter.addAction(ACTION_TICK)
        filter.addAction(App.ACTION_UPDATE)
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    override fun onStartListening() {
        if (!app.timerRunning) {
            setOffState()
        }
    }

    override fun onClick() {
        app.cycle()
    }

    private fun setToState(state: WakeState) {
        updateTileWithNewInfo(
                resources.getText(state.label),
                Icon.createWithResource(this, state.icon),
                if (state.time == App.ZERO_MIN) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        )
    }

    private fun updateTileWithNewInfo(label: CharSequence, icon: Icon, state: Int) {
        qsTile?.label = label
        qsTile?.icon = icon
        qsTile?.state = state
        qsTile?.updateTile()
    }

    private fun setOffState() {
        updateTileWithNewInfo(
                resources.getText(STATE_OFF.label),
                Icon.createWithResource(this, STATE_OFF.icon),
                Tile.STATE_INACTIVE
        )
    }

    private fun onTick(millisUntilFinished: Long) {
        qsTile?.label = String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
        )
        qsTile?.updateTile()
    }
}