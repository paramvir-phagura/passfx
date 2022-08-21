package com.passfx.view

import com.passfx.model.Database
import com.passfx.model.io.DatabaseStorageType
import javafx.geometry.Insets
import javafx.geometry.Pos
import com.passfx.util.okButton
import tornadofx.*

class DatabasePropertiesView : Fragment() {
    private val database: Database by param()

    override val root = vbox {
        gridpane {
            row {
                label("${database.name} Properties") {
                    styleClass += "header-label"
                    gridpaneConstraints {
                        columnSpan = 2
                    }
                }
            }
            row {
                label("Number of accounts:")
                label("${database.accounts.size}")
            }
            row {
                label("Method of persistence:")
                label("${DatabaseStorageType.getFor(database.persistence::class)?.desc}")
            }
            row {
                label("Revision:")
                label("${database.revision}")
            }
            row {
                label("Remote location:")
                label(database.remoteLocation)
            }
            vgap = 10.0
            constraintsForColumn(0).percentWidth = 75.0
            constraintsForColumn(1).percentWidth = 25.0
            isGridLinesVisible = false
        }
        hbox {
            okButton { close() }
            paddingTop = 15.0
            alignment = Pos.CENTER
        }
        padding = Insets(10.0, 10.0, 10.0, 10.0)
    }
}