package com.passfx.util.config

import com.passfx.util.UserConfiguration
import org.junit.Test

class JsonConfigTest {
    @Test
    fun loadConfig() {
        println(UserConfiguration.INSTANCE.initialDatabase)
    }
}