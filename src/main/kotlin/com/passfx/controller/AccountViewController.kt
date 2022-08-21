package com.passfx.controller

import javafx.scene.control.TextInputControl
import com.passfx.util.Clipboard
import com.passfx.view.AccountView
import tornadofx.Controller

class AccountViewController : Controller() {
    private val view: AccountView by inject()

    fun copyFrom(control: TextInputControl) {
        Clipboard.copy(control.text)
    }

    fun pasteTo(control: TextInputControl) {
        control.text = Clipboard.paste()
    }

    fun handleEnter() {
        ok()
    }

    fun ok() {
        if (view.commit()) {
            view.close()
        }
    }

    fun cancel() {
        view.close()
    }
}