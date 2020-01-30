package net.upm.util.config.imex

import net.upm.model.Account
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
        val db = lastPassImex.import()
        db.accounts.forEach { println("${it.name}\n\t${it.username}\n\t${it.password}\n\t${it.url}") }
    }

    val line = "https://github.com/,an_email@domain.com,S6-Mlmk,,GitHub,Development,0"

    @Test
    fun importAccount()
    {
        val acc = convert(line)
        println(acc.name.value)
        println(acc.username.value)
        println(acc.password.value)
        println(acc.url.value)
    }


    // Split line based of every 6 commas
    // line = line.trim()
    // for (i = 0; i < 6; i++)
    //  strIndex = str.indexOf(",", strIndex)
    // line = str.substring(strIndex)
    // val acc = convert(line)
    fun splitLine()
    {

    }

    fun convert(line: String): Account
    {
        var remaining = line
        val values = mutableListOf<String>()

        while (remaining.isNotEmpty())
        {
            // Index to split before
            var splitIndex = remaining.indexOf(",")
            if (splitIndex == -1)
                splitIndex = remaining.length
            var startIndex = 0
            if (remaining.startsWith("\""))
            {
                splitIndex = remaining.indexOf(",", remaining.indexOf("\"", 1)) - 1
                startIndex = 1
            }

            val value = remaining.substring(startIndex, splitIndex)
            values += value
            if (remaining.indexOf(",") != -1)
                splitIndex++
            remaining = remaining.substring(splitIndex)
        }

        val acc = Account(values[4], values[1], values[2], values[0], values[3])
        return acc
    }
}