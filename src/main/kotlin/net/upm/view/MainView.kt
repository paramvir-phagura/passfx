package net.upm.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.*
import javafx.scene.image.ImageView
import net.upm.controller.MainViewController
import net.upm.model.Account
import net.upm.model.Database
import net.upm.util.database
import tornadofx.*

class MainView : View("UPM")
{
    private val controller: MainViewController by inject()

    val databaseTabPane = TabPane()
    val accountsView = ListView<Account>()
    val searchField = TextField()
    val statusLabel = Label("Get started by creating or opening a database")

    val currentDatabaseSelection
        get() = databaseTabPane.selectionModel.selectedItem?.database
    val currentAccountSelection
        get() = accountsView.selectionModel.selectedItemProperty()?.value

    // TODO Short circuit boolean binding
    val accountSelected = accountsView.selectionModel.selectedItemProperty().isNotNull
    val databaseSelected = databaseTabPane.selectionModel.selectedItemProperty().isNotNull
    val openableUrl = SimpleBooleanProperty(false)
    val copyableUsername = SimpleBooleanProperty(false)
    val copyablePassword = SimpleBooleanProperty(false)

    override val root = borderpane {
        /**
         * Menu
         */
        top = vbox {
            menubar {
                menu(messages["mainView.databaseMenu"]) {
                    item(messages["mainView.newDatabaseItem"]).action { controller.newDatabase() }
                    item(messages["mainView.openDatabaseItem"]).action { controller.openDatabase() }
                    item(messages["mainView.openDatabaseUrlItem"]).isDisable = true
                    item("Close Database") {
                        enableWhen(databaseSelected)
                        action { controller.closeTab() }
                    }
                    separator()
                    item("Sync").isDisable = true
                    item("Change Password") {
                        enableWhen(databaseSelected)
                        action { controller.changePassword() }
                    }
                    item("Database Properties") {
                        enableWhen(databaseSelected)
                        action { controller.databaseProperties() }
                    }
                    separator()
                    item("Export").isDisable = true
                    item("Import") {
                        action { controller.import() }
                    }
                    separator()
                    item("Quit UPM").action { controller.quit() }
                }

                menu("Account") {
                    item("Add Account") {
                        enableWhen(databaseSelected)
                        action { controller.newAccount() }
                    }
                    item("Edit Account") {
                        enableWhen(accountSelected)
                        action { controller.editAccount(currentAccountSelection!!) }
                    }
                    item("Delete Account") {
                        enableWhen(accountSelected)
                        action { controller.deleteAccount(currentDatabaseSelection!!, currentAccountSelection!!) }
                    }
                    item("View Account") {
                        enableWhen(accountSelected)
                        action { controller.viewAccount(currentAccountSelection!!) }
                    }
                    item("Copy Username") {
                        enableWhen(copyableUsername)
                        action { controller.copyUsername(currentAccountSelection!!) }
                    }
                    item("Copy Password") {
                        enableWhen(copyablePassword)
                        action { controller.copyPassword(currentAccountSelection!!) }
                    }
                    item("Launch URL") {
                        enableWhen(openableUrl)
                        action { controller.launchUrl() }
                    }
                }

                menu("Help") {
                    item("About").action { controller.showAboutView() }
                }

//                val os = System.getProperty("os.name")
//                if (os != null && os.startsWith("Mac"))
//                {
//                    useSystemMenuBarProperty().value = true
//                }
            }
            /**
             * Account mutation toolbar
             */
            toolbar {
                button("", ImageView("images/add-account.png")) {
                    enableWhen(databaseSelected)
                    action { controller.newAccount() }
                }
                button("", ImageView("images/edit-account.png")) {
                    enableWhen(accountSelected)
                    action { controller.editAccount(currentAccountSelection!!) }
                }
                button("", ImageView("images/delete-account.png")) {
                    enableWhen(accountSelected)
                    action { controller.deleteAccount(currentDatabaseSelection!!, currentAccountSelection!!) }
                }
                separator()
                button("", ImageView("images/copy-username.png")) {
                    enableWhen(copyableUsername)
                    action { controller.copyPassword(currentAccountSelection!!) }
                }
                button("", ImageView("images/copy-password.png")) {
                    tooltip("Copy password")
                    enableWhen(copyablePassword)
                    action { controller.copyPassword(currentAccountSelection!!) }
                }
                button("", ImageView("images/open-url.png")) {
                    enableWhen(openableUrl)
                    action { controller.launchUrl() }
                }
                separator()
                button("", ImageView("images/sync.png")).isDisable = true
                button("", ImageView("images/settings.png")) {
                    action { controller.showSettingsView() }
                }
            }
            /**
             * Search/filter toolbar
             */
            toolbar {
                imageview("images/search.png")
                searchField.textProperty().addListener { _, _, newValue ->
                    controller.search(newValue)
                }
                searchField.enableWhen(databaseSelected)
                add(searchField)
                button("", ImageView("images/clear-search.png")) {
                    enableWhen(searchField.textProperty().isNotEmpty)
                    action { controller.clearSearch() }
                }
            }

            add(databaseTabPane)
        }
        /**
         * Accounts view
         */
        center = accountsView
        /**
         * Status label
         */
        bottom = statusLabel
    }

    init
    {
        accountSelected.addListener { _, _, newValue ->
            if (newValue)
                openableUrl.value = currentAccountSelection!!.openableUrl
            else
                openableUrl.value = false
        }
    }

    override fun onBeforeShow()
    {
        // Init the controller
        controller
    }

    override fun onDock()
    {
        controller.loadInitialDatabase()
    }

    fun newTab(db: Database): Tab
    {
        val tab = Tab(db.name)
        tab.database = db

        return tab
    }
}

