package net.upm.view

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.image.ImageView
import javafx.scene.text.Font
import net.upm.util.GITHUB_URL
import net.upm.util.okButton
import net.upm.util.openUrl
import tornadofx.*

class AboutView : View("About")
{
    override val root = vbox {
        text("Made with ❤ by Pavan") {
            styleClass += "header-label"
            font = Font.font("Courier New", 36.0)
        }
        text("A fork of the Universal Password Manager project by Adrian Smith.")
        hbox {
            button("", ImageView("images/github.png")).action { openUrl(GITHUB_URL) }
            alignment = Pos.CENTER
            spacing = 50.0
        }
        okButton { close() }
        alignment = Pos.CENTER
        padding = Insets(25.0, 25.0, 25.0, 25.0)
        spacing = 15.0
    }

    override fun onBeforeShow()
    {
        currentStage!!.isResizable = false
        root.requestFocus()
    }
}