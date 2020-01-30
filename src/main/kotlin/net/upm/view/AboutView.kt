package net.upm.view

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.image.ImageView
import javafx.scene.text.Font
import net.upm.util.okButton
import tornadofx.*

class AboutView : View("About")
{
    override val root = vbox {
        text("Made with ❤ by Pavan") {
            styleClass += "header-label"
            font = Font.font("Courier New", 36.0)
        }
        text("A fork of the original UPM project by Adrian Smith.")
        hbox {
            button("", ImageView("images/github.png"))
            button("", ImageView("images/twitter.png"))
            button("", ImageView("images/reddit.png"))
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