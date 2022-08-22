package com.passfx.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import javafx.concurrent.Task
import com.passfx.model.Account
import com.passfx.model.Database
import org.slf4j.LoggerFactory
import tornadofx.ItemViewModel
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.reflect.KClass

sealed class IMEX {
    open val import: Task<List<Account>>? = null

    abstract fun import(): Task<List<Account>>

    abstract fun export(vararg dbs: Database)

    class Model : ItemViewModel<IMEX>() {
        var imex: ItemViewModel<out IMEX>? = null
    }
}

enum class SupportedIMEX(
    /** The class supporting the IMEX. */
    val imex: KClass<out IMEX>,
    /** The formal name of the service for which IMEX is supported. */
    val formalName: String
) {
    LASTPASS(LastPassIMEX::class, "LastPass"),
    BITWARDEN(BitwardenIMEX::class, "Bitwarden");

    override fun toString() = formalName

    companion object {
        fun forClass(clazz: KClass<out IMEX>) = values().firstOrNull { it.imex == clazz }
    }
}

class LastPassIMEX private constructor() : IMEX() {
    var data = ""

    constructor(data: String) : this() {
        this.data = data
    }

    override fun import() : Task<List<Account>> {
        return object : Task<List<Account>>() {
            override fun call(): List<Account> {
                return convert(data)
            }
        }
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

    companion object {
        private val log = LoggerFactory.getLogger(LastPassIMEX::class.java)
    }

    class Model : ItemViewModel<LastPassIMEX>(LastPassIMEX()) {
        val data = bind(LastPassIMEX::data)
    }
}

class BitwardenIMEX private constructor() : IMEX() {
    private val gson = GsonBuilder()
        .excludeFieldsWithoutExposeAnnotation()
        .setPrettyPrinting()
        .create()

    var file = ""

    constructor(file: String) : this() {
        this.file = file
    }

    override fun import() : Task<List<Account>> {
        return object : Task<List<Account>>() {
            override fun call(): List<Account> {
                val path = Paths.get(file)
                val accounts = mutableListOf<Account>()

                if (Files.exists(path)) {
                    val data = gson.fromJson(Files.newBufferedReader(path), JsonObject::class.java)

                    if (data != null) {
                        val items = data.getAsJsonArray("items")
                        val size = items.size()

                        items.map { it.asJsonObject }.forEachIndexed { idx, item ->
                            val type = item["type"].asInt
                            when (type) {
                                1 -> {
                                    val name = item["name"].asString
                                    val notes = if (item["notes"].isJsonNull) null else item["notes"].asString

                                    val login = item["login"].asJsonObject
                                    val username =
                                        if (login["username"].isJsonNull) null else login["username"].asString
                                    val password =
                                        if (login["password"].isJsonNull) null else login["password"].asString
                                    val uris =
                                        if (!item.has("uris") || login["uris"].isJsonNull) null
                                        else login["uris"].asJsonArray
                                    val urls = uris?.map { it.asJsonObject }?.map { it["uri"].asString }

                                    val account = Account(
                                        name,
                                        username ?: "",
                                        password ?: "",
                                        urls?.firstOrNull() ?: "",
                                        notes ?: ""
                                    )
                                    accounts += account

                                    updateProgress(idx.toDouble(), size.toDouble())
                                    Thread.sleep(10)
                                }
                            }
                        }
                        updateProgress(size.toDouble(), size.toDouble())
                        log.info("Imported ${accounts.size} accounts.")
                    }
                }
                return accounts
            }
        }
    }

    override fun export(vararg dbs: Database) {
        TODO("Not yet implemented")
    }

    companion object {
        private val log = LoggerFactory.getLogger(BitwardenIMEX::class.java)
    }

    class Model : ItemViewModel<BitwardenIMEX>(BitwardenIMEX()) {
        val file = bind(BitwardenIMEX::file)
    }
}