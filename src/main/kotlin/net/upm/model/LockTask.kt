package net.upm.model

import net.upm.isInFocus
import java.util.*

class LockTask(val duration: Int, private val incrementBy: Int = 1) : TimerTask() {
    var elapsed = 0

    override fun run() {
        if (isInFocus)
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