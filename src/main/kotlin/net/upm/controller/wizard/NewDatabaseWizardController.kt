package net.upm.controller.wizard

import net.upm.model.DuplicateDatabaseException
import net.upm.util.USER_HOME_FILE
import net.upm.util.previousDir
import net.upm.util.previousDirFile
import net.upm.view.wizard.StorageInput
import tornadofx.Controller
import tornadofx.chooseDirectory
import java.io.File

class StorageInputController : Controller() {

    private val view: StorageInput by inject()

    fun chooseDir(): File? {
        val dir = chooseDirectory("Select a location", previousDirFile ?: USER_HOME_FILE)
        val file = "${view.dbModel.name.value}.adb"

        if (dir != null) {
            val dirPath = dir.toPath()
            val db = File(dir.toPath().resolve(file).toUri())
            previousDir = dirPath

            if (db.exists()) {
                throw DuplicateDatabaseException("DB already exists")
            }
            return db
        }
        return null
    }
}