package net.upm.util.config.imex

import net.upm.util.imex.LastPassIMEX
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

class LastPassImexTest
{
    @Test
    fun import()
    {
        val path = Paths.get(this::class.java.getResource("/lastpass.txt").toURI())
        if (!Files.exists(path))
            return

        val data = String(Files.readAllBytes(path))
        val lastPassImex = LastPassIMEX(data)
        val accounts = lastPassImex.import()
        accounts.forEach { println("${it.name.value}\n\t${it.username.value}\n\t${it.password.value}\n\t${it.url.value}\n\t${it.notes.value}") }
        println("Imported ${accounts.size} accounts from LastPass.")
    }

    @Test
    fun splitLines()
    {
        val path = Paths.get(this::class.java.getResource("/lastpass.txt").toURI())
        if (!Files.exists(path))
            return

        val data = String(Files.readAllBytes(path))
        val lastPassImex = LastPassIMEX(data)
        val lines = lastPassImex.splitLines(data)
//        println(lines.size)
    }
}