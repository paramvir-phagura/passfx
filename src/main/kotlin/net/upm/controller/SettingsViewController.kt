package net.upm.controller

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.ButtonType
import net.upm.model.Database
import net.upm.util.chooseDatabase
import net.upm.util.config.UserConfiguration
import net.upm.view.SettingsView
import tornadofx.ChangeListener
import tornadofx.Controller
import tornadofx.information
import tornadofx.isDirty

class SettingsViewController : Controller() {

    private val view: SettingsView by inject()
    private val model: UserConfiguration.Model by inject()
    val shouldLock = SimpleBooleanProperty(UserConfiguration.INSTANCE.autoLock.value > 0)
    val expandHandler = ChangeListener<Number> { _, _, _ -> view.currentStage!!.sizeToScene() }

    operator fun invoke() {
        view.enableLock.selectedProperty().addListener { _, _, newValue ->
            if (newValue)
                view.autoLockField.text = "5"
            else
                view.autoLockField.text = "0"
        }
    }

    fun chooseInitialDb() {
        val file = chooseDatabase(view.currentStage!!) ?: return
        val path = file.toPath()
        model.initialDatabase.value = path.toString()
    }

    fun ok() {
        var restartPending = false

        if (model.language.isDirty)
            restartPending = true

        model.commit()
        view.close()

        if (shouldLock.value) {
            Database.setLockTimer(UserConfiguration.INSTANCE.autoLock.value)
        } else {
            Database.clearLockTimer()
        }

        if (restartPending) {
            information("Restart Pending",
                "PassFx must restart in order to apply the changes!",
                buttons = arrayOf(ButtonType.OK, ButtonType.CANCEL),
                owner = view.currentStage,
                title = "Warning",
                actionFn = { if (it == ButtonType.OK) Platform.exit() })
        }
    }

    fun cancel() {
        view.close()
    }
}