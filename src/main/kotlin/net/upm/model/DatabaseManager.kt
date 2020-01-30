package net.upm.model

import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import net.upm.util.config.UserConfiguration
import org.slf4j.LoggerFactory

object DatabaseManager
{
    private val log = LoggerFactory.getLogger(DatabaseManager::class.java)

    val databases: ObservableList<Database> = FXCollections.observableArrayList<Database>()

    init
    {
        databases.addListener { change: ListChangeListener.Change<out Database> ->
            change.next()
            if (change.wasAdded())
            {
                log.info("Database ${change.addedSubList.first().name} added.")
            } else if (change.wasRemoved())
            {
                val db = change.removed.first()
                db.save()
                log.info("Database ${db.name} removed.")
            }
        }

        Database.setLockTimer(UserConfiguration.INSTANCE.autoLock.value)
    }

    @Throws(DuplicateDatabaseException::class)
    operator fun plusAssign(database: Database)
    {
        check(database)
        databases.add(database)
    }

    @Throws(DuplicateDatabaseException::class)
    fun check(database: Database)
    {
        if (databases.contains(database))
            throw DuplicateDatabaseException("Database already exists")
    }

    fun saveAll()
    {
        databases.forEach { it.save() }
    }
}