package net.upm.model.io

import net.upm.model.Database
import org.junit.Test

class LocalDatabaseSerializationTest
{
    @Test
    fun loadDatabase()
    {
        println("Loading database...")
        val db = createDatabase()

        try
        {
            db.load()
            db.accounts.forEach { println("\t- ${it.name.value}") }
            saveDatabase(db)
        } catch(e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun saveDatabase(db: Database)
    {
        println("Saving database...")
        db.save()
    }

    fun createDatabase(): Database
    {
        val url = "/Users/pavanphagura/Desktop/Password Database"
        val password = "test"
        val persistence = LocalFileDatabasePersistence(url, password)
        val db = Database("TestDB", persistence)

        return db
    }
}