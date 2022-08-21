package com.passfx.model.io

import com.passfx.model.Database

class LocalDatabaseSerializationTest
{
//    @Test
    fun loadDatabase()
    {
        println("Loading database...")
        val db = createDatabase()

        try
        {
//            db.load()
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
//        db.save()
    }

    fun createDatabase(): Database
    {
        val dir = "/Users/pavanphagura/Desktop"
        val password = "test"
        val persistence = LocalFileDatabasePersistence(dir, password)
        val db = Database("TestDB", persistence)

        return db
    }
}