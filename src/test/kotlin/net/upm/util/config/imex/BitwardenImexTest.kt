package net.upm.util.config.imex

import com.google.gson.GsonBuilder
import javafx.application.Platform
import net.upm.util.imex.BitwardenIMEX
import org.junit.BeforeClass
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

class BitwardenImexTest {

    private val gson = GsonBuilder()
        .excludeFieldsWithoutExposeAnnotation()
        .setPrettyPrinting()
        .create()

    private val path = Paths.get("bitwarden.json")

    @Test
    fun import() {
        if (Files.exists(path)) {
            Platform.runLater {
                val imex = BitwardenIMEX(path.toString())
                imex.import.start()
//                imex.importService.progressProperty().addListener { _, _, newValue ->
//                    println("Progress $newValue")
//                }
                imex.import.setOnSucceeded {
                    imex.import.value.apply {
                        forEach {
                            println("${it.name.value}\n\t${it.username.value}\n\t${it.password.value}\n\t${it.url.value}\n\t${it.notes.value}")
                        }
                    }
                }
            }
        }
        Thread.sleep(2500)
    }

    companion object {
        @BeforeClass @JvmStatic
        fun pre() {
            FxDummy.initFx()
        }
    }
}