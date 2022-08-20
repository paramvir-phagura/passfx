package net.upm.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import net.upm.controller.MainViewController
import net.upm.model.Account
import net.upm.model.Database
import net.upm.util.database
import tornadofx.*

class MainView : View("PassFx") {
    private val controller: MainViewController by inject()

    val databaseTabPane = TabPane()
    val accountsView = ListView<Account>()
    val searchField = TextField()
    val sortBox = ComboBox<AccountSort>()
    val statusBar = HBox()
    val statusLabel = Label("Get started by creating or opening a database")
    val progressBar = ProgressBar()
    val progressLabel = Label()

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
                menu(messages["databaseMenu"]) {
                    item(messages["newDatabaseMenuItem"]).action { controller.newDatabase() }
                    item(messages["openDatabaseMenuItem"]).action { controller.openDatabase() }
                    item(messages["importMenuItem"]).action { controller.import() }
                    item(messages["exportMenuItem"]).isDisable = true
                    item("Close Database") {
                        enableWhen(databaseSelected)
                        action { controller.closeTab() }
                    }
                    separator()
                    item(messages["syncWithRemoteDatabaseMenuItem"]) {
                        enableWhen(databaseSelected)
                        action { controller.sync() }
                    }
                    item(messages["changeMasterPasswordMenuItem"]) {
                        enableWhen(databaseSelected)
                        action { controller.changePassword() }
                    }
                    item(messages["databasePropertiesMenuItem"]) {
                        enableWhen(databaseSelected)
                        action { controller.databaseProperties() }
                    }
                    separator()
                    item(messages["exitMenuItem"]).action { controller.quit() }
                }

                menu(messages["accountMenu"]) {
                    item(messages["addAccountMenuItem"]) {
                        enableWhen(databaseSelected)
                        action { controller.newAccount() }
                    }
                    item(messages["editAccountMenuItem"]) {
                        enableWhen(accountSelected)
                        action { controller.editAccount(currentAccountSelection!!) }
                    }
                    item(messages["deleteAccountMenuItem"]) {
                        enableWhen(accountSelected)
                        action { controller.deleteAccount(currentDatabaseSelection!!, currentAccountSelection!!) }
                    }
                    item(messages["viewAccountMenuItem"]) {
                        enableWhen(accountSelected)
                        action { controller.viewAccount(currentAccountSelection!!) }
                    }
                    item(messages["copyUsernameMenuItem"]) {
                        enableWhen(copyableUsername)
                        action { controller.copyUsername(currentAccountSelection!!) }
                    }
                    item(messages["copyPasswordMenuItem"]) {
                        enableWhen(copyablePassword)
                        action { controller.copyPassword(currentAccountSelection!!) }
                    }
                    item(messages["launchURLMenuItem"]) {
                        enableWhen(openableUrl)
                        action { controller.launchUrl(currentAccountSelection!!) }
                    }
                }

                menu(messages["helpMenu"]) {
                    item(messages["optionsMenuItem"]).action { controller.showSettingsView() }
                    item(messages["aboutMenuItem"]).action { controller.showAboutView() }
                }

                // Doesn't work
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
                    action { controller.launchUrl(currentAccountSelection!!) }
                }
                separator()
                button("", ImageView("images/sync.png")) {
                    enableWhen(databaseSelected)
                    action { controller.sync() }
                }
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
                region {
                    hgrow = Priority.ALWAYS
                }
                imageview("images/sort.png")
                sortBox.items = AccountSort.values().asList().asObservable()
                sortBox.selectionModel.selectFirst()
                sortBox.valueProperty().addListener { _, _, _ -> controller.sort(sortBox.value.comparator) }
                add(sortBox)
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
        bottom = statusBar.apply {
            add(statusLabel)
            region { hgrow = Priority.ALWAYS }
            add(progressLabel)
        }
    }

    init {
        accountSelected.addListener { _, _, newValue ->
            if (newValue)
                openableUrl.value = currentAccountSelection!!.openableUrl
            else
                openableUrl.value = false
        }
    }

    override fun onBeforeShow() {
        // Init the controller
        controller
    }

    override fun onDock() {
        // Required here otherwise onDock() isn't called
        statusBar.add(progressBar)
        controller.loadInitialDatabase()
    }

    fun newTab(db: Database): Tab {
        val tab = Tab()
        tab.textProperty().bind(db.nameProp)
        tab.contextMenu = ContextMenu().apply {
            item("Rename").action {
                controller.rename()
            }
            item("Close").action {
                controller.closeTab()
            }
        }
        tab.database = db

        return tab
    }
}
