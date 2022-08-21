package com.passfx.model.io

import kotlin.reflect.KClass

enum class DatabaseStorageType {
    LOCAL {
        override val persistence = LocalFileDatabasePersistence::class
        override val desc = "Local Database"
    };

    /**
     * The method of persistence.
     */
    abstract val persistence: KClass<out DatabasePersistence>

    /**
     * A description of the method.
     */
    abstract val desc: String

    override fun toString() = desc

    companion object {
        fun getFor(method: KClass<out DatabasePersistence>) =
            values().filter { it.persistence == method }.firstOrNull()
    }
}