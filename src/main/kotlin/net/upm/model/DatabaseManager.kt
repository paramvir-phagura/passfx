package net.upm.model

import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import net.upm.util.TaskScheduler
import net.upm.util.config.UserConfiguration
import org.slf4j.LoggerFactory

object DatabaseManager {
    private val log = LoggerFactory.getLogger(DatabaseManager::class.java)

    val databases: ObservableList<Database> = FXCollections.observableArrayList()

    init {
        databases.addListener { change: ListChangeListener.Change<out Database> ->
            change.next()
            if (change.wasAdded()) {
                change.addedSubList.forEach { database ->
                    database.nameProp.addListener { _, _, _ ->
                        TaskScheduler.submitSync(database.persistence.delete())
                        TaskScheduler.submitSync(database.persistence.serialize())
                    }
                    log.info("Database \"${database.name}\" added.")
                }
            } else if (change.wasRemoved()) {
                val database = change.removed.first()
//                submitSync(database.persistence.serialize())
                log.info("Database \"${database.name}\" removed.")
            }
        }

        Database.setLockTimer(UserConfiguration.INSTANCE.autoLock.value)
    }

    @Throws(DuplicateDatabaseException::class)
    operator fun plusAssign(database: Database) {
        check(database)
        databases.add(database)
    }

    operator fun minusAssign(database: Database) {
        databases.remove(database)
    }

    @Throws(DuplicateDatabaseException::class)
    fun check(database: Database) {
        if (databases.contains(database))
            throw DuplicateDatabaseException("Database \"${database.name}\" already exists")
    }

    fun saveAll() {
        databases.forEach {
            TaskScheduler.submitAsync(it.persistence.serialize())
        }
    }
}