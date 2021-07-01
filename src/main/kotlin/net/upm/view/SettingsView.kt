package net.upm.view

import javafx.geometry.Pos
import javafx.scene.control.Accordion
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import javafx.scene.control.TitledPane
import net.upm.controller.SettingsViewController
import net.upm.util.cancelButton
import net.upm.util.config.UserConfiguration
import net.upm.util.maskableTextField
import net.upm.util.okButton
import tornadofx.*

class SettingsView : View("Settings") {
    private val controller: SettingsViewController by inject()
    private val model: UserConfiguration.Model by inject()

    lateinit var settings: Accordion
    lateinit var general: TitledPane
    lateinit var enableLock: CheckBox
    lateinit var autoLockField: TextField

    override val root = vbox {
        /**
         * General
         */
        general = titledpane("General") {
            gridpane {
                // Database
                row {
                    label("Database Options") {
                        styleClass += "header-label"
                        gridpaneConstraints {
                            marginBottom = 5.0
                        }
                    }
                }
                row {
                    label("Database to load on startup:")
                }
                row {
                    textfield(model.initialDatabase) {
                        textProperty().bindBidirectional(model.initialDatabase)
                        isEditable = false
                    }
                    button("...") {
                        action { controller.chooseInitialDb() }
                        gridpaneConstraints {
                            marginLeft = 10.0
                        }
                    }
                }
                // Window
                row {
                    label("Window") {
                        styleClass += "header-label"
                        gridpaneConstraints {
                            marginTop = 10.0
                            marginBottom = 5.0
                        }
                    }
                }
                row {
                    label("Language:")
                }
                row {
                    combobox(values = listOf("English")) {
                        gridpaneConstraints {
                            columnSpan = 2
                            useMaxWidth = true
                        }
                    }.value = "English"
                }
                row {
                    enableLock = checkbox("Lock when out of focus (minutes):") {
                        selectedProperty().bindBidirectional(controller.shouldLock)
                    }
                    autoLockField = textfield(model.autoLock) {
                        enableWhen(enableLock.selectedProperty())
                    }
                }
                row {
                    checkbox("Always on top", model.alwaysOnTop)
                }
                // Password
                row {
                    label("Password") {
                        styleClass += "header-label"
                        gridpaneConstraints {
                            marginTop = 10.0
                            marginBottom = 5.0
                        }
                    }
                }
                row {
                    label("Length of generated passwords:")
                    textfield()
                }
                row {
                    checkbox("Include symbols")
                }
                row {
                    checkbox("Hide password by default").selectedProperty().bindBidirectional(model.hidePassword)
                }

                constraintsForColumn(0).percentWidth = 85.0
                constraintsForColumn(1).percentWidth = 15.0
                isGridLinesVisible = false
            }
        }

        settings = accordion {
            panes.add(general)
            /**
             * HTTP
             */
            fold("HTTP", gridpane()) {
                row {
                    label("Proxy") {
                        styleClass += "header-label"
                        gridpaneConstraints {
                            marginBottom = 5.0
                        }
                    }
                }
                row {
                    checkbox("Enable proxy").isDisable = true
                }
                row {
                    label("Proxy:")
                    label("Port:")
                }
                row {
                    textfield().enableWhen(UserConfiguration.INSTANCE.proxy)
                    textfield {
                        gridpaneConstraints {
                            marginLeft = 10.0
                        }
                        enableWhen(UserConfiguration.INSTANCE.proxy)
                    }
                }
                row {
                    label("Username:")
                }
                row {
                    textfield {
                        enableWhen(UserConfiguration.INSTANCE.proxy)
                        gridpaneConstraints {
                            columnSpan = 2
                        }
                    }
                }
                row {
                    label("Password:")
                }
                row {
                    maskableTextField {
                        enableWhen(UserConfiguration.INSTANCE.proxy)
                        gridpaneConstraints {
                            columnSpan = 2
                        }
                        maxWidth = Double.MAX_VALUE
                    }
                }
                row {
                    checkbox("Accept self-signed certificates").isDisable = true
                }
                constraintsForColumn(0).percentWidth = 65.0
                constraintsForColumn(1).percentWidth = 35.0
            }
            heightProperty().addListener(controller.expandHandler)
        }
        /**
         * Ok/cancel
         */
        hbox {
            okButton { controller.ok() }
            cancelButton { controller.cancel() }
            paddingTop = 15.0
            paddingBottom = 15.0
            alignment = Pos.CENTER
            spacing = 15.0
        }
    }

    override fun onBeforeShow() {
        controller()
        currentStage!!.maxWidth = 375.0
        settings.expandedPane = general
    }
}