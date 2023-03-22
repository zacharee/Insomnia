package com.zacharee1.insomnia.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper

val Context.eventManager: EventManager
    get() = EventManager.getInstance(this)

class EventManager private constructor(context: Context) : ContextWrapper(context) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: EventManager? = null

        @Synchronized
        fun getInstance(context: Context): EventManager {
            return instance ?: EventManager(context.applicationContext ?: context).apply {
                instance = this
            }
        }
    }

    private val listeners = HashSet<EventListener>()

    fun addListener(listener: EventListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: EventListener) {
        listeners.remove(listener)
    }

    fun sendEvent(event: Event) {
        listeners.forEach { it.onEvent(event) }
    }
}

sealed class Event {
    data class NewState(val state: WakeState) : Event()
    data class Tick(val remaining: Long): Event()

    object Cycle : Event()
    object RequestUpdate : Event()
}

interface EventListener {
    fun onEvent(event: Event)
}
