package com.passfx.view.wizard

import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import com.passfx.model.Account
import com.passfx.util.maskableTextField
import tornadofx.*

class NewAccountWizard : Wizard("New Account Wizard", "Add an account containing credentials.") {
    val accountModel: Account.Model by inject()

    // TODO Place in controller
    private val enterHandler = EventHandler<KeyEvent> {
        if (it.code == KeyCode.ENTER) {
            // Why is this not a method?
            currentPage.onSave()
            if (currentPage.isComplete) {
                onSave()
                if (isComplete)
                    close()
            }
        }
    }

    override val canGoNext = currentPageComplete
    override val canFinish = allPagesComplete

    init {
        add(BasicAccountInfoView::class, mapOf("enterHandler" to enterHandler))
    }

    override fun onSave() {
        isComplete = accountModel.commit()
    }
}

class BasicAccountInfoView : View("Info") {
    private val accountModel: Account.Model by inject()

    private val enterHandler: EventHandler<KeyEvent> by param()

    override val complete = accountModel.valid(accountModel.name)

    override val root = form {
        fieldset(title) {
            field("Account") {
                textfield(accountModel.name) {
                    required()
                    onKeyPressed = enterHandler
                }
            }
            field("Username") {
                textfield(accountModel.username).onKeyPressed = enterHandler
            }
            field("Password") {
                maskableTextField(accountModel.password).onKeyPressed = enterHandler
            }
            field("URL") {
                textfield(accountModel.url).onKeyPressed = enterHandler
            }
            field("Notes") {
                textarea(accountModel.notes) {
                    onKeyPressed = enterHandler
                    prefWidth = 250.0
                    prefHeight = 75.0
                }
            }
        }
    }
}