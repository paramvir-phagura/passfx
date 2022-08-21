package com.passfx.controller.wizard

import com.passfx.util.USER_HOME_FILE
import com.passfx.util.previousDir
import com.passfx.view.wizard.ImportWizard
import javafx.stage.FileChooser
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