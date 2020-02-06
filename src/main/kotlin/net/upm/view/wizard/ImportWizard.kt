package net.upm.view.wizard

import javafx.scene.layout.VBox
import net.upm.model.Database
import net.upm.util.imex.IMEX
import net.upm.util.imex.LastPassIMEX
import net.upm.util.imex.SupportedIMEX.LASTPASS
import net.upm.util.imex.SupportedIMEX.values
import net.upm.util.toggleMap
import tornadofx.*

class ImportWizard : Wizard("Import Wizard", "Import data from another password manager.")
{
    val dbModel: Database.Model by inject()
    val imexModel: IMEX.Model by inject()

    override val canGoNext = currentPageComplete
    override val canFinish = allPagesComplete

    init
    {
        add(ImportView::class)
        add(GeneralInput::class)
        add(StorageInput::class)
        add(ReviewView::class)
    }

    override fun onSave()
    {
        try
        {
            val db = dbModel.item!!
            val persistence = dbModel.persistenceModel!!
            val accounts = imexModel.type!!.item.import()
            dbModel.item.persistence = persistence.item
            persistence.item.database = dbModel.item
            db.accounts.addAll(accounts)

            isComplete = dbModel.commit() && persistence.commit() && imexModel.commit()
        } catch (e: Exception)
        {
            e.printStackTrace()
        }
    }
}

class ImportView : View("Import")
{
    private val dbModel: Database.Model by inject()
    private val imexModel: IMEX.Model by inject()

    override val root = form {
        fieldset {
            field("From") {
                combobox(values = values().asList()) {
                    toggleMap {
                        LASTPASS toggles {
                            VBox().apply {
                                val type = find<LastPassIMEX.Model>()
                                imexModel.type = type

                                currentStage!!.width = 465.0
                                currentStage!!.height = 455.0

                                val dataArea = textarea(type.data)
                                dataArea.required()

                                label("For instructions on how to import from LastPass, visit...")

                                imexModel.type!!.valid(type.data).addListener { _, _, newValue -> isComplete = newValue }
                            }
                        }
                    }
                    toggleMap.parent = this@form
                }
            }
        }
    }

    override fun onSave()
    {
        imexModel.type!!.commit()
        super.onSave()
    }
}

class ReviewView : View("Review")
{
    override val root = form {

    }
}