package net.upm.controller.wizard

import javafx.stage.FileChooser
import net.upm.util.USER_HOME_FILE
import net.upm.util.previousDir
import net.upm.view.wizard.ImportWizard
import tornadofx.Controller
import java.io.File

class ImportWizardController : Controller() {
    private val view: ImportWizard by inject()

    fun chooseFile(): File? {
        val fileChooser = FileChooser()
        fileChooser.title = "Select your Bitwarden database"
        fileChooser.initialDirectory = USER_HOME_FILE
        fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("Bitwarden export", "*", "*.json"))

        val file = fileChooser.showOpenDialog(view.currentStage)
        previousDir = file?.parentFile?.toPath() ?: previousDir
        return file
    }
}