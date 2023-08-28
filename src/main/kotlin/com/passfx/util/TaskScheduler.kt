package com.passfx.util

import javafx.concurrent.Task
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object TaskScheduler {
    private val executor = Executors.newSingleThreadExecutor()

    fun <T> submitSync(init: Task<T>.() -> Unit = {}, task: Task<T>) : Task<T> {
        init(task)
        executor.submit(task)
        return task
    }

    fun <T> submitSync(init: Task<T>.() -> Unit = {}, action: (Task<T>) -> T?) : Task<T> {
        val task = object : Task<T>() {
            override fun call() : T? {
                return action(this)
            }
        }
        return submitSync(init, task)
    }

    fun <T> submitSync(task: Task<T>) : Task<T> {
        return submitSync(init = {}, task)
    }

    // TODO
    fun <T> submitAsync(init: Task<T>.() -> Unit = {}, task: Task<T>) : Task<T> {
        return submitSync(init, task)
    }

    fun <T> submitAsync(init: (Task<T>) -> Unit = {}, action: (Task<T>) -> T?) : Task<T> {
        val task = object : Task<T>() {
            override fun call() : T? {
                return action(this)
            }
        }
        return submitAsync(init, task)
    }

    fun <T> submitAsync(task: Task<T>) : Task<T> {
        return submitAsync({}, task)
    }

    fun close() {
        executor.shutdown()
        executor.awaitTermination(60, TimeUnit.SECONDS)
    }
}