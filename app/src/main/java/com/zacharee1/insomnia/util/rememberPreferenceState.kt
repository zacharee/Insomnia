package com.zacharee1.insomnia.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.preference.PreferenceManager

@Composable
fun <T> Context.rememberPreferenceState(
    key: String,
    value: () -> T,
    onChanged: (T) -> Unit
): MutableState<T> {
    val state = remember(key) {
        mutableStateOf(value())
    }

    LaunchedEffect(key1 = state.value) {
        onChanged(state.value)
    }

    DisposableEffect(key1 = key) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, k ->
            if (key == k) {
                state.value = value()
            }
        }

        PreferenceManager.getDefaultSharedPreferences(this@rememberPreferenceState)
            .registerOnSharedPreferenceChangeListener(listener)

        onDispose {
            PreferenceManager.getDefaultSharedPreferences(this@rememberPreferenceState)
                .unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    return state
}