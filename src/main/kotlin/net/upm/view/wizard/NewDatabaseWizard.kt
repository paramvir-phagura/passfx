package net.upm.view.wizard

import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import net.upm.controller.wizard.StorageInputController
import net.upm.model.Database
import net.upm.model.io.DatabaseStorageType.LOCAL
import net.upm.model.io.DatabaseStorageType.values
import net.upm.model.io.LocalFileDatabasePersistence
import net.upm.util.maskableTextField
import net.upm.util.toggleMap
import tornadofx.*

class NewDatabaseWizard
    : Wizard("New Database Wizard", "Create a new database for your accounts.")
{
    val dbModel: Database.Model by inject()

    override val canGoNext = currentPageComplete
    override val canFinish = allPagesComplete

    init
    {
        add(GeneralInput::class)
        add(StorageInput::class)
//        add(EncryptionInput::class)
    }

    override fun onSave()
    {
        val persistence = dbModel.persistenceModel!!
        dbModel.item.persistence = persistence.item
        persistence.item.database = dbModel.item

        isComplete = dbModel.commit() && persistence.commit()
    }
}

class GeneralInput : View("General")
{
    private val dbModel: Database.Model by inject()

    override val complete = dbModel.valid(dbModel.name)

    override val root = form {
        fieldset(title) {
            field("Name") {
                textfield(dbModel.name).required()
            }
            label("e.g., Personal, Family, Work, or School")
        }
    }
}

class StorageInput : View("Storage")
{
    val dbModel: Database.Model by inject()
    private val controller: StorageInputController by inject()

    init
    {
        isComplete = false
    }

    override val root = form {
        fieldset(title) {
            field("Type") {
                combobox(values = values().asList()) {
                    toggleMap {
                        LOCAL toggles {
                            Fieldset("Location").apply {
                                val persistence = find<LocalFileDatabasePersistence.Model>()
                                dbModel.persistenceModel = persistence

                                currentStage!!.width = 480.0
                                currentStage!!.height = 485.0

                                field("URL") {
                                    hbox {
                                        val dirProp = SimpleStringProperty()
                                        dirProp.bindBidirectional(persistence.dir)

                                        val dirField = textfield()
                                        dirField.hgrow = Priority.ALWAYS

                                        button("...").action {
                                            val file = controller.chooseDir()
                                            if (file != null)
                                            {
                                                val dir = file.parentFile
                                                dirProp.value = dir.toString()
                                                dirField.text = file.toString()
                                                dirField.positionCaret(dirField.text.length)
                                            }
                                        }

                                        spacing = 10.0
                                    }
                                }
                                field("Password") {
                                    maskableTextField(persistence.password).required()
                                }

                                dbModel.persistenceModel!!.valid(persistence.dir, persistence.password)
                                        .addListener { _, _, newValue -> isComplete = newValue }
                            }
                        }

                        toggleMap.parent = this@form
                        toggleMap.empty = Label("Select a storage method for the data.")
                    }
                }
            }
        }
    }
}

class EncryptionInput : View("Encryption")
{
    override val root = fieldset(title) {
        TODO("Not implemented.")
    }
}