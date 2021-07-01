package net.upm.view

import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.control.TextInputControl
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import net.upm.controller.AccountViewController
import net.upm.model.Account
import net.upm.util.cancelButton
import net.upm.util.maskableTextField
import net.upm.util.okButton
import tornadofx.*

class AccountView : View() {
    private val controller: AccountViewController by inject()
    private val account: Account by param()
    private val accountModel = Account.Model(account)
    private val editable: Boolean by param()
    private val passwordField = TextField()
    private val enterHandler = EventHandler { e: KeyEvent ->
        if (e.code == KeyCode.ENTER)
            controller.handleEnter()
    }

    override val root = form {
        fieldset(account.name.value) {
            field("Username:") {
                val input = textfield(accountModel.username) {
                    prefWidth = 250.0
                    isEditable = editable
                    onKeyPressed = enterHandler
                }
                copyButton(input)
                pasteButton(input)
            }
            field("Password:") {
                val input = maskableTextField(account.password, keyHandler = enterHandler) {
                    isEditable = editable
                }
                copyButton(input)
                pasteButton(input)
            }
            field("URL:") {
                val input = textfield(account.url) {
                    prefWidth = 250.0
                    isEditable = editable
                    onKeyPressed = enterHandler
                }
                copyButton(input)
                pasteButton(input)
            }
            field("Notes:") {
                val input = textarea(account.notes) {
                    prefWidth = 250.0
                    prefHeight = 75.0
                    isEditable = editable
                    onKeyPressed = enterHandler
                }
                copyButton(input)
                pasteButton(input)
            }
            hbox {
                okButton { controller.ok() }
                cancelButton { controller.cancel() }
                paddingTop = 15.0
                alignment = Pos.CENTER
                spacing = 15.0
            }
        }
//        padding = Insets(10.0, 10.0, 0.0, 10.0)
    }

    init {
        title = account.name.value
    }

    override fun onDock() {
        // Remove focus from the first textfield
        root.requestFocus()
    }

    fun commit(): Boolean {
        return accountModel.commit()
    }

    fun EventTarget.copyButton(input: TextInputControl): Button {
        val copyButton = Button("", ImageView("images/copy-icon.png")).attachTo(this)
        copyButton.action {
            controller.copyFrom(input)
        }
        return copyButton
    }

    fun EventTarget.pasteButton(input: TextInputControl) =
        Button("", ImageView("images/paste-icon.png")).attachTo(this).apply {
            disableProperty().value = !editable
            action {
                controller.pasteTo(input)
            }
        }
}