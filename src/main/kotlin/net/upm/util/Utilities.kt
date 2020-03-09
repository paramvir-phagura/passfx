package net.upm.util

import javafx.stage.FileChooser
import javafx.stage.Stage
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.nio.file.Path

val USER_HOME: String = System.getProperty("user.home")
val USER_HOME_FILE = File(USER_HOME)

/**
 * The previously accessed directory.
 */
var previousDir: Path? = null
val previousDirFile: File?
    get() = previousDir?.toFile()

fun chooseDatabase(owner: Stage): File?
{
    val fileChooser = FileChooser()
    fileChooser.title = "Select an account database"
    fileChooser.initialDirectory = previousDirFile ?: USER_HOME_FILE
    fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("Account DB", "*", "*.adb"))

    val file = fileChooser.showOpenDialog(owner)
    previousDir = file?.parentFile?.toPath() ?: previousDir

    return file
}

fun openUrl(url: String)
{
    if (!url.contains("http"))
        return

    try
    {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
            Desktop.getDesktop().browse(URI.create(url))
    } catch(e: Exception)
    {
        e.printStackTrace()
    }
}