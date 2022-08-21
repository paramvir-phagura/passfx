package com.passfx.util

import javafx.stage.FileChooser
import javafx.stage.Stage
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

val USER_HOME: String = System.getProperty("user.home")
val USER_HOME_FILE = File(USER_HOME)

val initialDatabaseDirectory =
    if (UserConfiguration.INSTANCE.initialDatabase.value.isEmpty())
        null
    else
        Paths.get(
            UserConfiguration.INSTANCE.initialDatabase.value.substring(0,
            UserConfiguration.INSTANCE.initialDatabase.value.lastIndexOf("\\")))

/**
 * The previously accessed directory.
 */
var previousDir: Path? = null
val previousDirFile: File?
    get() = previousDir?.toFile()

fun chooseDatabase(owner: Stage): File? {
    var dir = previousDirFile ?: initialDatabaseDirectory?.toFile() ?: USER_HOME_FILE
    if (!Files.exists(dir.toPath())) {
        dir = USER_HOME_FILE
    }
    // log.debug("Directory $dir")

    val fileChooser = FileChooser()
    fileChooser.title = "Select an account database"
    fileChooser.initialDirectory = dir
    fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("Account DB", "*", "*.adb"))

    val file = fileChooser.showOpenDialog(owner)
    previousDir = file?.parentFile?.toPath() ?: previousDir

    return file
}

fun openUrl(url: String) {
    if (!url.contains("http"))
        return

    try {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
            Desktop.getDesktop().browse(URI.create(url))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}