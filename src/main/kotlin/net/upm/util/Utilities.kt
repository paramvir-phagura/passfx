package net.upm.util

import javafx.stage.FileChooser
import javafx.stage.Stage
import java.awt.Desktop
import java.io.File
import java.net.URI

fun chooseDatabase(owner: Stage): File?
{
    val fileChooser = FileChooser()
    fileChooser.title = "Select an account database"
    fileChooser.initialDirectory = File(USER_HOME)
    fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("Account DB", "*", "*.adb"))

    return fileChooser.showOpenDialog(owner)
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