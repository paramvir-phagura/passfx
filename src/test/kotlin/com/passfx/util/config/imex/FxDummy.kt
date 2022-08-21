package com.passfx.util.config.imex

import javafx.application.Application
import javafx.stage.Stage

class FxDummy : Application() {

    override fun start(primaryStage: Stage?) {
    }

    companion object {
        @JvmStatic
        fun initFx() {
            val t = object : Thread() {
                override fun run() {
                    launch(FxDummy::class.java)
                }
            }
            t.isDaemon = true
            t.start()
            Thread.sleep(500)
        }
    }
}