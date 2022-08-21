package com.passfx.util.config.imex

import org.junit.BeforeClass
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

class LastPassImexTest {
    private val path = Paths.get("lastpass.txt")

    @Test
    fun import() {
//        if (!Files.exists(path))
//            return
//
//        val data = String(Files.readAllBytes(path))
//        val lastPassImex = LastPassIMEX(data)
//        lastPassImex.import.start()
//        lastPassImex.import.setOnSucceeded {
//            lastPassImex.import.value.apply {
//                forEach {
//                    println("${it.name.value}\n\t${it.username.value}\n\t${it.password.value}\n\t${it.url.value}\n\t${it.notes.value}")
//                }
//                println("Imported $size accounts from LastPass.")
//            }
//        }
    }

    @Test
    fun splitLines() {
        if (!Files.exists(path))
            return

//        val data = String(Files.readAllBytes(path))
//        val lastPassImex = LastPassIMEX(data)
//        val lines = lastPassImex.splitLines(data)
//        println(lines)
    }

    companion object {
        @BeforeClass
        @JvmStatic
        fun pre() {
            FxDummy.initFx()
        }
    }
}