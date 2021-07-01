package net.upm.util.imex

import javafx.beans.property.SimpleDoubleProperty
import net.upm.model.Account
import net.upm.model.Database
import tornadofx.ItemViewModel
import kotlin.reflect.KClass

sealed class IMEX {
    val progress = SimpleDoubleProperty(100.0)

    abstract fun import(): List<Account>

    abstract fun export(vararg dbs: Database)

    class Model : ItemViewModel<IMEX>() {
        var type: ItemViewModel<out IMEX>? = null
    }
}

enum class SupportedIMEX(val formalName: String, val klass: KClass<out IMEX>) {
    LASTPASS("LastPass", LastPassIMEX::class);

    override fun toString() = formalName
}

class LastPassIMEX private constructor() : IMEX() {
    var data = ""

    constructor(data: String) : this() {
        this.data = data
    }

    override fun import(): List<Account> {
        return convert(data)
    }

    fun splitLines(data: String) {
        val lines = mutableListOf<String>()
        var remaining = data

        while (remaining.isNotEmpty()) {
            var lineSplitIndex = 0

            for (i in 0..6) {
                if (i == 6) {
                    lineSplitIndex = remaining.indexOf("\n")
                    break
                }

                var valueIndex = remaining.indexOf(",", lineSplitIndex)

                val value = remaining.substring(lineSplitIndex, valueIndex)
                if (value.startsWith("\"")) {
                    valueIndex = remaining.indexOf("\"", lineSplitIndex)
                }

                lineSplitIndex = valueIndex
                println(valueIndex)
            }

            val line = remaining.substring(0, lineSplitIndex)
//            println("line $line")
            remaining = remaining.substring(lineSplitIndex)
        }

        println("Converted ${lines.size} lines.")
    }

    // TODO Make this more modular
    fun convert(data: String): List<Account> {
        val accounts = mutableListOf<Account>()
        var remaining = data

        while (remaining.isNotEmpty()) {
            val values = mutableListOf<String>()

            for (i in 0..6) {
                var startIndex = 0
                var splitIndex = remaining.indexOf(",")
                val newLineIndex = remaining.indexOf("\n")
                var mod = 1 // For values enclosed in apostrophes

                if (splitIndex == -1)
                    splitIndex = remaining.length
                else if (newLineIndex != -1 && newLineIndex < splitIndex) {
                    println(true)
                    splitIndex = newLineIndex
                }
                if (remaining.startsWith("\"")) {
                    splitIndex = remaining.indexOf("\"", 1)
                    startIndex++
                    mod++
                }

                val value = remaining.substring(startIndex, splitIndex)
                values += value

                if (remaining.indexOf(",") != -1)
                    splitIndex += mod
                remaining = remaining.substring(splitIndex)
            }


            val acc = Account(values[4], values[1], values[2], values[0], values[3])
            accounts += acc
        }
        return accounts
    }

    override fun export(vararg dbs: Database) {
        TODO("Not implemented.")
    }

    class Model : ItemViewModel<LastPassIMEX>(LastPassIMEX()) {
        val data = bind(LastPassIMEX::data)
    }
}