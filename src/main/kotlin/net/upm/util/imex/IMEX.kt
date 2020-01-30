package net.upm.util.imex

import net.upm.model.Account
import net.upm.model.Database
import net.upm.model.io.LocalFileDatabasePersistence
import tornadofx.asObservable

sealed class IMEX
{
    abstract fun import(): Database

    abstract fun export(vararg dbs: Database)
}

class LastPassIMEX(private val data: String) : IMEX()
{
    override fun import(): Database
    {
        // TODO Split lines every 6th occurrence of ","
        val lines = data.split("\n")
        val accounts = mutableListOf<Account>().asObservable()
        lines.forEachIndexed { index, line ->
            // Ignore template
            if (index == 0)
                return@forEachIndexed

            val account = convert(line)
            accounts += account
            println("Converted account ${account.name.value}.")
        }
        val db = Database("LastPass", LocalFileDatabasePersistence("", ""))
        db.accounts.addAll(accounts)
        return db
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

    override fun export(vararg dbs: Database)
    {
    }
}