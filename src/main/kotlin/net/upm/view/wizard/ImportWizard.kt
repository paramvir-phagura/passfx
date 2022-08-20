package net.upm.view.wizard

import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.VBox
import net.upm.controller.wizard.ImportWizardController
import net.upm.model.Database
import net.upm.model.io.DatabaseStorageType
import net.upm.util.imex.BitwardenIMEX
import net.upm.util.imex.IMEX
import net.upm.util.imex.LastPassIMEX
import net.upm.util.imex.SupportedIMEX
import net.upm.util.imex.SupportedIMEX.*
import net.upm.util.toggleMap
import tornadofx.*

class ImportWizard : Wizard("Import Wizard", "Import data from another password manager.") {
    val dbModel: Database.Model by inject()
    val imexModel: IMEX.Model by inject()

    override val canGoNext = currentPageComplete
    override val canFinish = allPagesComplete

    init {
        add(ImportView::class)
        add(GeneralInput::class)
        add(StorageInput::class)
        add(ReviewView::class)
    }

    override fun onSave() {
        try {
            val persistence = dbModel.persistenceModel!!
            dbModel.item.persistence = persistence.item
            persistence.item.database = dbModel.item

            isComplete = dbModel.commit() && persistence.commit() && imexModel.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class ImportView : View("Import") {
    private val dbModel: Database.Model by inject()
    private val imexModel: IMEX.Model by inject()
    private val controller: ImportWizardController by inject()

    override val root = form {
        fieldset("Import") {
            field("From") {
                combobox(values = values().asList()) {
                    toggleMap {
                        LASTPASS toggles {
                            VBox().apply {
                                val type = find<LastPassIMEX.Model>()
                                imexModel.imex = type

                                currentStage!!.width = 465.0
                                currentStage!!.height = 455.0

                                val dataArea = textarea(type.data)
                                dataArea.required()

                                label("For instructions on how to import from LastPass, visit...")

                                imexModel.imex!!.valid(type.data)
                                    .addListener { _, _, newValue -> isComplete = newValue }
                            }
                        }
                        BITWARDEN toggles {
                            fieldset("Bitwarden Settings") bitwarden@ {
                                val type = find<BitwardenIMEX.Model>()
                                imexModel.imex = type

                                currentStage!!.width = 465.0
                                currentStage!!.height = 455.0

                                field("Path") {
                                    hbox {
                                        val pathField = textfield(type.file)
                                        pathField.required()

                                        button("Open").action {
                                            val file = controller.chooseFile()
                                            if (file != null) {
                                                pathField.text = file.toString()
                                                pathField.positionCaret(pathField.text.length)
                                            }
                                        }

                                        spacing = 10.0
                                    }
                                }

                                label("For instructions on how to import from Bitwarden, visit...") {
                                    isWrapText = true
                                }

                                imexModel.imex!!.valid(type.file)
                                    .addListener { _, _, newValue -> isComplete = newValue }
                            }
                        }
                    }
                    toggleMap.parent = this@form
                }
            }
        }
    }

    init {
        isComplete = false
    }

    override fun onSave() {
        imexModel.imex!!.commit()
        super.onSave()
    }
}

class ReviewView : View("Review") {
    private val dbModel: Database.Model by inject()
    private val imexModel: IMEX.Model by inject()

    private val nameDesc = SimpleStringProperty()
    private val importDesc = SimpleStringProperty()

    override val root = form {
        fieldset(title) {
            spacing = 25.0
            label(importDesc) {
                isWrapText = true
            }
            label(nameDesc) {
                isWrapText = true
            }
        }
    }

    override fun onDock() {
        super.onDock()
        importDesc.value = "You are importing data from ${SupportedIMEX.forClass(imexModel.imex!!.item!!::class)}."
        nameDesc.value = "A new database will be created as a " +
                "${DatabaseStorageType.getFor(dbModel.persistenceModel!!.item!!::class)} under the name \"${dbModel.name.value}\"."
    }
}