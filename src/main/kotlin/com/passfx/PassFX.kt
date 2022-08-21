package com.passfx

import com.passfx.model.Database
import com.passfx.model.DatabaseManager
import com.passfx.model.Language
import com.passfx.util.TaskScheduler
import com.passfx.util.UserConfiguration
import com.passfx.view.MainView
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.image.Image
import javafx.stage.Stage
import org.slf4j.LoggerFactory
import tornadofx.App
import tornadofx.FX
import tornadofx.importStylesheet
import java.util.*

class PassFX : App(MainView::class) {
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
        super.stop()
        // TODO These should be tasks
        Database.closeTimer()
        DatabaseManager.saveAll()
        UserConfiguration.INSTANCE.save()
        TaskScheduler.close()
    }

    companion object {
        private val log = LoggerFactory.getLogger(PassFX::class.java)

        private val focusProperty = SimpleBooleanProperty(false)

        val isInFocus: Boolean
            get() = focusProperty.value
    }
}