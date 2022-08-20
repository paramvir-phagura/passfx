package net.upm.util.config

import org.junit.Test

class JsonConfigTest {
    @Test
    fun loadConfig() {
        println(UserConfiguration.INSTANCE.initialDatabase)
    }
}