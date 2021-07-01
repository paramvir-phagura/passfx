package net.upm.view

import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import net.upm.util.cancelButton
import net.upm.util.maskableTextField
import net.upm.util.okButton
import tornadofx.*

class InputDialog() : Fragment("Password") {
    private val text: String by param()
    private val mask: Boolean by param()
    private lateinit var input: TextField

    lateinit var value: String
        private set
    var canceled = false
        private set

    override val root = vbox {
        label(text)

        input = maskableTextField(maskPassword = mask, withToggle = mask, keyHandler = EventHandler { e ->
            if (e.code == KeyCode.ENTER)
                submit()
        })
        hbox {
            okButton({ submit() }) {
                enableWhen(input.textProperty().isNotEmpty)
            }
            cancelButton { submit(cancel = true) }
            paddingTop = 5.0
            alignment = Pos.CENTER
            spacing = 15.0
        }
        padding = Insets(10.0, 10.0, 15.0, 10.0)
    }

    private fun submit(cancel: Boolean = false) {
        if (!input.text.isNullOrEmpty()) {
            value = input.text
        }
        canceled = cancel
        close()
    }
}