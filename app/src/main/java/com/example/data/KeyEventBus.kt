package com.example.data

import android.view.KeyEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class HardwareKeyEvent(
    val keyCode: Int,
    val isDown: Boolean,
    val keyName: String,
    val timestamp: Long = System.currentTimeMillis()
)

object KeyEventBus {
    private val _events = MutableSharedFlow<HardwareKeyEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<HardwareKeyEvent> = _events.asSharedFlow()

    fun onKeyEvent(keyCode: Int, isDown: Boolean): Boolean {
        val name = when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> "Volume Up"
            KeyEvent.KEYCODE_VOLUME_DOWN -> "Volume Down"
            KeyEvent.KEYCODE_POWER -> "Power"
            KeyEvent.KEYCODE_CAMERA -> "Camera Key"
            KeyEvent.KEYCODE_HEADSETHOOK -> "Headset Hook"
            KeyEvent.KEYCODE_BACK -> "Back Key"
            else -> KeyEvent.keyCodeToString(keyCode)
        }
        _events.tryEmit(HardwareKeyEvent(keyCode, isDown, name))
        // Consume volume keys during test so they don't change volume or interrupt audio
        return keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
    }
}
