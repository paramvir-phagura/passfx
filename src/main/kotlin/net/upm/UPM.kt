package net.upm

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.image.Image
import javafx.stage.Stage
import net.upm.model.Database
import net.upm.model.DatabaseManager
import net.upm.model.Language
import net.upm.util.config.UserConfiguration
import net.upm.view.MainView
import org.slf4j.LoggerFactory
import tornadofx.App
import tornadofx.FX
import tornadofx.importStylesheet
import java.util.*

class UPM : App(MainView::class) {
    init {
        FX.messages = ResourceBundle.getBundle("upm", Locale(Language.valueOf(UserConfiguration.INSTANCE.language.value.uppercase()).code))
        importStylesheet(UserConfiguration.INSTANCE.theme.value)
    }

    override fun start(stage: Stage) {
        stage.width = 465.0
        stage.height = 500.0
        stage.icons.addAll(Image("images/icon.png"))

        focusProperty.bind(stage.focusedProperty())
        stage.isAlwaysOnTop = UserConfiguration.INSTANCE.alwaysOnTop.value
        UserConfiguration.INSTANCE.alwaysOnTop.addListener { _, _, newValue -> stage.isAlwaysOnTop = newValue }

        super.start(stage)
    }

    override fun stop() {
        Database.closeTimer()
        DatabaseManager.saveAll()
        UserConfiguration.INSTANCE.save()
        super.stop()
    }

    companion object {
        private val log = LoggerFactory.getLogger(UPM::class.java)
    }
}

private val focusProperty = SimpleBooleanProperty(false)

val isInFocus: Boolean
    get() = focusProperty.value