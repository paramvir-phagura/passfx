package com.passfx.model

import com.passfx.PassFX
import java.util.*

class LockTask(val duration: Int, private val incrementBy: Int = 1) : TimerTask() {
    var elapsed = 0

    override fun run() {
        if (PassFX.isInFocus)
            return

        elapsed += incrementBy
        if (elapsed >= duration) {
            DatabaseManager.databases.forEach { it.lock() }
            reset()
        }
    }

    private fun reset() {
        elapsed = 0
    }
}