package net.upm.util

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.input.KeyEvent
import net.upm.model.Database
import net.upm.util.config.UserConfiguration
import org.slf4j.LoggerFactory
import tornadofx.*
import kotlin.reflect.KProperty

object TabDatabaseProvider
{
    private val dbs = mutableMapOf<Tab, Database>()

    operator fun getValue(tab: Tab, property: KProperty<*>) = dbs[tab]!!

    operator fun setValue(tab: Tab, property: KProperty<*>, db: Database)
    {
        dbs[tab] = db
    }
}

var Tab.database: Database by TabDatabaseProvider

var <T> ComboBox<T>.toggleMap: ComboBoxToggleMap<T>
    get()
    {
        return ComboBoxToggleMap.getFor(this)
    }
    set(newValue)
    {
        ComboBoxToggleMap.cboxRegistry[this] = newValue
    }

fun <T> ComboBox<T>.toggleMap(fn: ComboBoxToggleMap<T>.() -> Unit) = toggleMap.also(fn)

class ComboBoxToggleMap<T> private constructor(combobox: ComboBox<T>)
{
    companion object
    {
        private val log = LoggerFactory.getLogger(this::class.java)
        internal val cboxRegistry = FXCollections.observableHashMap<ComboBox<*>, ComboBoxToggleMap<*>>()
        val empty: Node = Label("Nothing here")

        init
        {
            cboxRegistry.addListener registry@{ change: MapChangeListener.Change<out ComboBox<*>, out ComboBoxToggleMap<*>> ->
                log.debug("Added? ${change.wasAdded()} / Removed? ${change.wasRemoved()}")
                val cbox = change.key

                if (change.wasAdded())
                {
                    cbox.valueProperty().addListener combobox@{ _, oldValue, newValue ->
                        if (oldValue == newValue)
                            return@combobox
                        val toggle = change.map[cbox]!!
                        val parent = toggle.parent ?: cbox.parent

                        log.debug("Switching to $newValue from $oldValue")
                        log.debug("Previous node removed? ${toggle.onRemoveNode(parent)}")
                        log.debug("Next node toggled? ${toggle.onAddNode(toggle.toggles[newValue]!!(), parent)}")
                    }
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun <T> getFor(cbox: ComboBox<T>) = (cboxRegistry[cbox] ?: ComboBoxToggleMap(cbox)) as ComboBoxToggleMap<T>
    }

    private val toggles = HashMap<T, () -> Node>()
    var parent: Parent? = null
    private var lastToggle: Node? = null

    var onAddNode: (Node, Parent) -> Boolean = { node, parent ->
        parent.add(node)
        lastToggle = node
        true
    }

    var onRemoveNode: (Parent) -> Boolean = { parent ->
        val member = parent::class.members.stream().filter { it.name == "getChildren" }.findFirst().get()
        val children = member.call(parent) as ObservableList<*>
        children.remove(lastToggle)
    }

    init
    {
        cboxRegistry[combobox] = this
    }

    infix fun <N : () -> Node> T.toggles(createNodeFn: N)
    {
        toggles[this] = createNodeFn
    }
}

fun EventTarget.maskableTextField(value: ObservableValue<String>? = null,
                                  maskPassword: Boolean = UserConfiguration.INSTANCE.hidePassword.value,
                                  keyHandler: EventHandler<KeyEvent>? = null,
                                  op: TextField.() -> Unit = {}): TextField
{
    val unmaskedField = TextField()
    val maskedField = PasswordField()
    val hidePassword = SimpleBooleanProperty(maskPassword)
    if (value != null)
        unmaskedField.bind(value)

    vbox {
        stackpane {
            unmaskedField.attachTo(this, op)
            maskedField.attachTo(this, op)
            maskedField.textProperty().bindBidirectional(unmaskedField.textProperty())
            maskedField.visibleProperty().bind(hidePassword)
            unmaskedField.visibleProperty().bind(hidePassword.not())
            if (keyHandler != null)
            {
                unmaskedField.onKeyPressed = keyHandler
                maskedField.onKeyPressed = keyHandler
            }
        }
        checkbox("Hide Password") {
            isSelected = hidePassword.value
            disableProperty().bind(unmaskedField.disableProperty())
            action {
                hidePassword.value = hidePassword.not().value
                if (hidePassword.value)
                {
                    maskedField.requestFocus()
                    maskedField.positionCaret(maskedField.text.length)
                } else
                {
                    unmaskedField.requestFocus()
                    if (!unmaskedField.text.isNullOrEmpty())
                        unmaskedField.positionCaret(unmaskedField.text.length)
                }
            }
        }
    }
    return unmaskedField
}

fun EventTarget.okButton(action: () -> Unit, op: Button.() -> Unit) =
        Button("Ok").attachTo(this, op).apply {
            styleClass += "ok-button"
            action(action)
        }

fun EventTarget.okButton(op: () -> Unit) = okButton(op, {})

fun EventTarget.cancelButton(op: () -> Unit) =
        button("Cancel") {
            prefWidth = 85.0
            action(op)
        }