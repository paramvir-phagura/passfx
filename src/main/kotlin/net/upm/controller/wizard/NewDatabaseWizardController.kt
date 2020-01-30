package net.upm.controller.wizard

import javafx.scene.control.TextInputControl
import net.upm.view.wizard.StorageInput
import tornadofx.Controller
import tornadofx.chooseDirectory
import java.io.File
import java.nio.file.Files

class StorageInputController : Controller()
{
    private val view: StorageInput by inject()

    fun chooseDir(dirField: TextInputControl)
    {
        val dir = chooseDirectory("Select a location",
                File(System.getProperty("user.home")))
        val file = "${view.dbModel.name.value}.adb"

        if (dir != null)
        {
            val filePath = dir.toPath().resolve(file)

            if (Files.exists(filePath))
            {
                println("DB already exists")
            } else
            {
                val path = filePath.toString()
                dirField.textProperty().value = path
                dirField.positionCaret(path.length)
            }
        } else
        {
            println("Invalid directory")
        }
    }
}