package com.zacharee1.insomnia.tiles

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.zacharee1.insomnia.App.Companion.ZERO_MIN
import com.zacharee1.insomnia.R
import com.zacharee1.insomnia.util.Event
import com.zacharee1.insomnia.util.EventListener
import com.zacharee1.insomnia.util.WakeState
import com.zacharee1.insomnia.util.eventManager
import java.util.concurrent.TimeUnit

class CycleTile : TileService(), EventListener {
    override fun onCreate() {
        eventManager.addListener(this)
    }

    override fun onDestroy() {
        eventManager.removeListener(this)
    }

    override fun onEvent(event: Event) {
        when (event) {
            is Event.NewState -> setToState(event.state)
            is Event.Tick -> onTick(event.remaining)
            else -> {}
        }
    }

    override fun onStartListening() {
        eventManager.sendEvent(Event.RequestUpdate)
    }

    override fun onClick() {
        eventManager.sendEvent(Event.Cycle)
    }

    private fun setToState(state: WakeState) {
        updateTileWithNewInfo(
                state.createLabelFromTime(this),
                Icon.createWithResource(this, state.drawableResourceFromTime()),
                if (state.time == ZERO_MIN) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        )
    }

    private fun updateTileWithNewInfo(label: CharSequence, icon: Icon, state: Int) {
        qsTile?.label = label
        qsTile?.icon = icon
        qsTile?.state = state
        qsTile?.updateTile()
    }

    private fun onTick(millisUntilFinished: Long) {
        updateTileWithNewInfo(
            label = String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))),
            state = if (millisUntilFinished > 0) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            icon = Icon.createWithResource(this, if (millisUntilFinished > 0) R.drawable.on else R.drawable.off)
        )
    }
}