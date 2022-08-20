package net.upm.util.config

import com.google.gson.*
import com.google.gson.annotations.Expose
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import net.upm.util.TaskScheduler
import org.slf4j.LoggerFactory
import tornadofx.ItemViewModel
import java.lang.reflect.Type
import java.nio.file.Files
import java.nio.file.Paths

sealed class Configuration {
    protected val settings = HashMap<String, String>()

    abstract fun save()
}

sealed class JsonConfiguration(fileName: String) : Configuration() {
    private val path = Paths.get("$CONFIG_DIR/$fileName.json")

    init {
        if (Files.exists(path)) {
            val config = gson.fromJson(Files.newBufferedReader(path), JsonObject::class.java)
            if (config != null) {
                for ((key, value) in config.asJsonObject.entrySet()) {
                    settings[key] = value.asString
                }
                log.info("Loaded $fileName config.")
            }
        }
    }

    override fun save() {
        TaskScheduler.submitAsync {
            Files.newBufferedWriter(path).use { gson.toJson(this, it) }
        }
    }

    private object ObservableValueSerializer : JsonSerializer<ObservableValue<Any>> {
        override fun serialize(
            src: ObservableValue<Any>?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return JsonPrimitive(src?.value.toString())
        }
    }

    companion object {
        private val CONFIG_DIR = System.getProperty("user.dir")

        private val log = LoggerFactory.getLogger(JsonConfiguration::class.java)

        protected val gson = GsonBuilder()
            .registerTypeAdapter(SimpleStringProperty::class.java, ObservableValueSerializer)
            .registerTypeAdapter(SimpleIntegerProperty::class.java, ObservableValueSerializer)
            .registerTypeAdapter(SimpleBooleanProperty::class.java, ObservableValueSerializer)
            .excludeFieldsWithoutExposeAnnotation()
            .setPrettyPrinting()
            .create()
    }
}

class UserConfiguration private constructor() : JsonConfiguration("app-preferences") {
    @Expose
    val theme = SimpleStringProperty(settings["theme"] ?: "/themes/material-design.css")
    @Expose
    val initialDatabase = SimpleStringProperty(settings["initialDatabase"] ?: "")
    @Expose
    val language = SimpleStringProperty(settings["language"] ?: "English")
    @Expose
    val autoLock = SimpleIntegerProperty(settings["autoLock"]?.toInt() ?: 0)
    @Expose
    val alwaysOnTop = SimpleBooleanProperty(settings["alwaysOnTop"]?.toBoolean() ?: false)
    @Expose
    val passwordGenLength = SimpleIntegerProperty(settings["passwordGenLength"]?.toInt() ?: 10)
    @Expose
    val includeSymbols = SimpleBooleanProperty(settings["includeSymbols"]?.toBoolean() ?: false)
    @Expose
    val hidePassword = SimpleBooleanProperty(settings["hidePassword"]?.toBoolean() ?: true)
    @Expose
    val acceptCertificates = SimpleBooleanProperty(settings["acceptCertificates"]?.toBoolean() ?: false)
    @Expose
    val proxy = SimpleBooleanProperty(settings["proxy"]?.toBoolean() ?: false)

    companion object {
        val INSTANCE = UserConfiguration()
    }

    class Model : ItemViewModel<UserConfiguration>(INSTANCE) {
        val initialDatabase = bind(UserConfiguration::initialDatabase)
        val language = bind(UserConfiguration::language)
        val autoLock = bind(UserConfiguration::autoLock)
        val alwaysOnTop = bind(UserConfiguration::alwaysOnTop)
        val hidePassword = bind(UserConfiguration::hidePassword)
    }
}