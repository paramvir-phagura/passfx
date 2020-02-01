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

class PromptPasswordFragment() : Fragment("Password")
{
    private val text: String by param()
    private lateinit var passwordInput: TextField

    lateinit var password: String
        private set
    var canceled = false
        private set

    override val root = vbox {
        label(text)

        passwordInput = maskableTextField(keyHandler = EventHandler { e ->
            if (e.code == KeyCode.ENTER)
                submit()
        })
        hbox {
            okButton({ submit() }) {
                enableWhen(passwordInput.textProperty().isNotEmpty)
            }
            cancelButton { submit(cancel = true) }
            paddingTop = 5.0
            alignment = Pos.CENTER
            spacing = 15.0
        }
        padding = Insets(10.0, 10.0, 15.0, 10.0)
    }

    fun submit(cancel: Boolean = false)
    {
        if (!passwordInput.text.isNullOrEmpty())
        {
            password = passwordInput.text
        }
        canceled = cancel
        close()
    }
}