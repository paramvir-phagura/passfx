package net.upm

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.image.Image
import javafx.stage.Stage
import net.upm.model.Database
import net.upm.model.DatabaseManager
import net.upm.util.config.UserConfiguration
import net.upm.view.MainView
import org.slf4j.LoggerFactory
import sun.misc.Unsafe
import tornadofx.App
import tornadofx.importStylesheet

class UPM : App(MainView::class)
{
    init
    {
        importStylesheet(UserConfiguration.INSTANCE.theme.value)
    }

    override fun start(stage: Stage)
    {
        stage.width = 465.0
        stage.height = 500.0
        stage.icons.addAll(Image("images/icon.png"))

        focusProperty.bind(stage.focusedProperty())
        stage.isAlwaysOnTop = UserConfiguration.INSTANCE.alwaysOnTop.value
        UserConfiguration.INSTANCE.alwaysOnTop.addListener { _, _, newValue -> stage.isAlwaysOnTop = newValue }

        super.start(stage)
    }

    override fun stop()
    {
        Database.closeTimer()
        DatabaseManager.saveAll()
        UserConfiguration.INSTANCE.save()
        super.stop()
    }

    companion object
    {
        private val log = LoggerFactory.getLogger(UPM::class.java)

        init
        {
            // Disable reflective access warnings
            try {
                val theUnsafe = Unsafe::class.java.getDeclaredField("theUnsafe");
                theUnsafe.isAccessible = true
                val u = theUnsafe.get(null) as Unsafe

                val cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
                val logger = cls.getDeclaredField("logger");
                u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

private val focusProperty = SimpleBooleanProperty(false)

val isInFocus: Boolean
    get() = focusProperty.value