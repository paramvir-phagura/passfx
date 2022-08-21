package com.passfx.controller

import com.passfx.model.Account
import com.passfx.model.Database
import com.passfx.model.DatabaseManager
import com.passfx.model.DuplicateDatabaseException
import com.passfx.model.io.DatabaseStorageType
import com.passfx.model.io.InvalidPasswordException
import com.passfx.model.io.LocalFileDatabasePersistence
import com.passfx.util.*
import com.passfx.view.*
import com.passfx.view.wizard.ImportWizard
import com.passfx.view.wizard.NewAccountWizard
import com.passfx.view.wizard.NewDatabaseWizard
import javafx.application.Platform
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.scene.control.ButtonType
import javafx.scene.control.ListCell
import javafx.stage.StageStyle
import org.slf4j.LoggerFactory
import tornadofx.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/** Controller for [MainView]. */
class MainViewController : Controller() {
    private val view: MainView by inject()

    private val emptyAccountsList = emptyList<Account>().asObservable()

    val progressProperty = view.progressBar.progressProperty()

    var progress: Double
        get() = progressProperty.get()
        set(newValue) { progressProperty.set(newValue) }

    init {
        // New database listener
        DatabaseManager.databases.addListener { change: ListChangeListener.Change<out Database> ->
            change.next()
            if (change.wasAdded()) {
                change.addedSubList.forEach { db ->
                    // Create tab for new database
                    val tab = view.newTab(db)
                    tab.setOnCloseRequest { closeDatabase(db) }
                    view.databaseTabPane.tabs.add(tab)
                    tab.select()

                    // Listen for lock toggles
                    db.lockedProp.addListener { _, _, newValue ->
                        if (newValue)
                            Platform.runLater {
                                val currentSelection = view.currentDatabaseSelection
                                if (currentSelection != null && currentSelection == db)
                                    refreshStatusLabel()
                            }
                    }
                }
            }
        }

        // Listen for changes in tab selection, change the accounts view accordingly
        view.databaseTabPane.selectionModel.selectedItemProperty().addListener { _, _, newTab ->
            if (newTab != null)
                sort()
            else
                view.accountsView.items = emptyAccountsList
            refreshStatusLabel()
        }

        // Enable/disable account mutation controls depending on the selected account
        view.accountsView.selectionModel.selectedItemProperty().addListener { _, _, _ ->
            updateAccountControls()
        }

        // Open the current account selection upon double click
        view.accountsView.setCellFactory {
            val cell = object : ListCell<Account>() {
                override fun updateItem(account: Account?, empty: Boolean) {
                    super.updateItem(account, empty)

                    if (empty || account == null) {
                        text = null
                        graphic = null
                    } else {
                        text = account.name.value
                    }
                }
            }
            cell.setOnMouseClicked { e ->
                if (e.clickCount >= 2 && cell.item != null) {
                    viewAccount(cell.item!!)
                }
            }
            return@setCellFactory cell
        }

        // Register a progress bar listener that automatically hides/shows it as needed
        // Additionally updates the progress label with appropriate text
        progressProperty.addListener { _, ov, nv ->
            if (ov == nv)
                return@addListener

            if (view.progressBar.progress <= 0.0 || view.progressBar.progress >= 1.0) {
                view.progressBar.hide()
                view.progressLabel.hide()
            } else {
                view.progressLabel.text = "${(nv as Double * 100).toInt()}%"
                view.progressBar.show()
                view.progressLabel.show()
            }
        }

        // Trigger the listener above to hide the progress bar
        clearProgress()
    }

    /** Open [NewDatabaseWizard] for database creation by the user. */
    fun newDatabase() {
        find(NewDatabaseWizard::class.java, scope = Scope()).apply {
            onComplete {
                DatabaseManager += dbModel.item
            }
            openModal()
        }
    }

    /** Open the database [file]. */
    fun openDatabase(file: File) {
        val dir = file.toPath().parent.toString()
        val fileName = file.nameWithoutExtension

        if (DatabaseManager.databases.any { it.name == fileName })
            tornadofx.error("Database already loaded!", buttons = arrayOf(ButtonType.OK), owner = view.primaryStage)
        else {
            try {
                promptPassword {
                    val db = Database(fileName, LocalFileDatabasePersistence(dir, it))
                    loadDatabase(db)
                }
            } catch (e: DuplicateDatabaseException) {
                tornadofx.error(e.message!!, buttons = arrayOf(ButtonType.OK), owner = view.primaryStage)
            } catch (e: Exception) {
                logger.error("Error loading database", e)
                tornadofx.error("Error", "Couldn't load database.", owner = view.currentStage)
                e.printStackTrace()
            }
        }
    }

    /** Prompt the user for a database file and open it. */
    fun openDatabase() {
        val file = chooseDatabase(view.currentStage!!) ?: return
        openDatabase(file)
    }

    fun openDatabaseFromURL() {
        TODO("Not implemented.")
    }

    /** Load [database] via its persistence method.*/
    @Throws(InvalidPasswordException::class, DuplicateDatabaseException::class, Exception::class)
    private fun loadDatabase(database: Database) {
        TaskScheduler.submitSync(init = {
            setOnSucceeded {
                DatabaseManager += database
            }
            bindProgressBar(this, "Loading database...")
        }, database.persistence.deserialize())
    }

    /** Open the database file specified in [UserConfiguration], if any. */
    fun loadInitialDatabase() {
        val url = UserConfiguration.INSTANCE.initialDatabase.value
        if (url.isNotBlank() && Files.exists(Paths.get(url))) {
            val file = File(url)
            openDatabase(file)
        }
    }

    fun reloadDatabase() {
        TODO("Not implemented.")
    }

    /** Close and save [database] via its persistence method. */
    fun closeDatabase(database: Database = view.currentDatabaseSelection!!) {
        sync(database)
        DatabaseManager.databases.remove(database)
    }

    /** Save the current database selection via its persistence method. */
    fun sync(database: Database = view.currentDatabaseSelection!!) {
        TaskScheduler.submitSync(init = {
            bindProgressBar(this, "Saving ${database.name}...")
        }, database.persistence.serialize())
    }

    /** Close the current tab and its attached database. */
    fun closeTab() {
        closeDatabase()
        view.databaseTabPane.selectionModel.selectedItem.close()
    }

    /** Prompt the user to rename the current database selection. */
    fun rename() {
        val db = view.currentDatabaseSelection!!

        promptInput {
            confirm(
                "Are you sure?",
                "The name of this database will be changed from ${db.name} to $it.",
                owner = view.currentStage
            ) {
                db.name = it
            }
        }
    }

    /** Prompt the user for a password change on the current [Database] selection. */
    fun changePassword() {
        val db = view.currentDatabaseSelection!!

        promptDatabasePassword(db) { // Current password
            promptPassword("Now enter a new password:") { newPassword ->
                promptPassword("Confirm your new password:") { confirm ->
                    comparePasswords(newPassword, confirm)
                    db.persistence.changePassword(newPassword)
                    information(
                        "Password changed!",
                        "The password for \"${db.name}\" has been successfully changed.",
                        ButtonType.OK,
                        owner = view.primaryStage
                    )
                }
            }
        }
    }

    /** View the properties of the current [Database] selection in [DatabasePropertiesView]. */
    fun databaseProperties() {
        val db = view.currentDatabaseSelection!!
        val propsView = find<DatabasePropertiesView>(params = mapOf("database" to db))
        propsView.openModal(StageStyle.UTILITY)
    }

    /**
     * Prompt the user for a password.
     * TODO Reuse last password, for opening multiple databases with the same pass without reprompt
     */
    private fun promptPassword(text: String = "Please enter a password:", actionFn: (String) -> Unit = {}) {
        val prompt = find<InputDialog>(params = mapOf("text" to text, "mask" to true))
        prompt.openModal(StageStyle.UTILITY, block = true)

        try {
            if (!prompt.canceled)
                actionFn(prompt.value)
        } catch (e: InvalidPasswordException) {
            invalidPasswordAlert(e)
            return promptPassword("Please reenter a password", actionFn)
        }
    }

    /** Prompt the user for a password and validate it against [database]. */
    private fun promptDatabasePassword(database: Database, actionFn: () -> Unit = {}) {
        promptPassword {
            database.persistence.checkPassword(it)
            actionFn()
        }
    }

    /** Prompt the user for a value. */
    private fun promptInput(text: String = "Please enter a new value", actionFn: (String) -> Unit = {}) {
        val prompt = find<InputDialog>(params = mapOf("text" to text, "mask" to false))
        prompt.openModal(StageStyle.UTILITY, block = true)

        if (!prompt.canceled)
            actionFn(prompt.value)
    }

    /** Display an error alert regarding an invalid password input. */
    fun invalidPasswordAlert(e: InvalidPasswordException? = null) {
        tornadofx.error(e?.message ?: "Invalid password!", buttons = arrayOf(ButtonType.OK), owner = view.primaryStage)
    }

    /** Compares the args and throws an [InvalidPasswordException] if they aren't equal. */
    @Throws(InvalidPasswordException::class)
    fun comparePasswords(input1: String, input2: String) {
        if (input1 != input2)
            throw InvalidPasswordException()
    }

    /**
     * Check if the provided [Database] is locked.
     * If it is, prompt the user for its password.
     * Otherwise allow access.
     */
    fun checkLock(db: Database): Boolean {
        if (db.locked) {
            promptPassword { password ->
                db.persistence.checkPassword(password)
                db.unlock(password)
            }
            return !db.locked
        }
        return true
    }

    private fun setStatusLabelText(text: String) {
        view.statusLabel.text = text
    }

    /** Update status label's text based off the state of the current database selection. */
    private fun refreshStatusLabel() {
        val builder = StringBuilder()
        val db = view.currentDatabaseSelection

        if (db != null) {
            builder.append(DatabaseStorageType.getFor(db.persistence::class)!!.desc)
            if (db.locked)
                builder.append(" (Locked)")
        }

        setStatusLabelText(builder.toString())
    }

    /** Open [ImportWizard] for importing data from another password manager. */
    @Suppress("UNCHECKED_CAST")
    fun import() {
        find(ImportWizard::class.java, scope = Scope()).apply {
            onComplete {
                val db = dbModel.item!!

                TaskScheduler.submitSync(init = {
                    setOnSucceeded {
                        val accounts = it.source.value as List<Account>
                        db.accounts.addAll(accounts)
                        DatabaseManager += db
                    }
                    bindProgressBar(this, "Importing database...")
                }, imexModel.imex!!.item.import())
            }
            openModal()
        }
    }

    fun export() {
        TODO("Not implemented.")
    }

    /** Open [NewAccountWizard] for account creation by the user. */
    fun newAccount() {
        if (!checkLock(view.currentDatabaseSelection!!))
            return

        find(NewAccountWizard::class.java, scope = Scope()).apply {
            onComplete {
                val db = view.currentDatabaseSelection!!
                val account = accountModel.item

                db.accounts += account
                view.accountsView.selectionModel.select(account)
                view.accountsView.scrollTo(account)
                refreshSearch()
                sort()
                logger.info("New account \"${accountModel.item.name.value}\" in database ${db.name}.")
            }
            openModal()
        }
    }

    /** View [account]'s details in [AccountView]. */
    fun viewAccount(account: Account, editable: Boolean = false) {
        if (!checkLock(view.currentDatabaseSelection!!))
            return

        find(
            AccountView::class.java,
            params = mapOf("account" to account, "editable" to editable),
            scope = Scope()
        ).apply {
            whenUndockedOnce {
                if (editable)
                    updateAccountControls()
            }
            openModal()
        }
    }

    /** Edit [account]'s details from the provided [Database]. */
    fun editAccount(account: Account) = viewAccount(account, true)

    /** Delete [account] from [database]. */
    fun deleteAccount(database: Database, account: Account) {
        if (!checkLock(view.currentDatabaseSelection!!))
            return

        val index = database.accounts.indexOf(account)
        database.accounts.remove(account)
        view.accountsView.refresh()
        refreshSearch()
        logger.info("Account \"${account.name.value}\" deleted.")

        // Select the very next account if the first account was deleted
        if (index == 0 && database.accounts.size > 0)
            view.accountsView.selectionModel.select(0)
    }

    /** Copy [account]'s username to the system clipboard. */
    fun copyUsername(account: Account) {
        if (!checkLock(view.currentDatabaseSelection!!))
            return

        Clipboard.copy(account.username.value)
    }

    /** Copy [account]'s password to the system clipboard. */
    fun copyPassword(account: Account) {
        if (!checkLock(view.currentDatabaseSelection!!))
            return

        Clipboard.copy(account.password.value)
    }

    /** Launch [account]'s URL in the default system browser. */
    fun launchUrl(account: Account) {
        openUrl(account.url.value)
    }

    /** Filter the accounts view items based on the provided predicate. */
    fun filter(db: Database, filter: (acc: Account) -> Boolean): ObservableList<Account> {
        return db.accounts.filter(filter).asObservable()
    }

    /** Filter the accounts view items based on the provided search entry. */
    fun search(query: String) {
        val queryMod = query.trim().lowercase()
        view.accountsView.items =
            filter(view.currentDatabaseSelection!!) { acc -> acc.name.value.lowercase().contains(queryMod) }
                .sorted(view.sortBox.value.comparator)
    }

    /** Re-search the current search query after a change in the accounts list. */
    fun refreshSearch() {
        search(view.searchField.text)
    }

    /** Clear the search field and entry. */
    fun clearSearch() {
        view.searchField.clear()
        view.accountsView.selectionModel.clearSelection()
    }

    /**
     * Sort the accounts view items based on the provided comparator.
     * This does not affect the backing list in [Database].
     */
    fun sort(comparator: Comparator<Account>) {
        clearSearch()
        view.accountsView.items = view.currentDatabaseSelection!!.accounts.sorted(comparator)
    }

    /**
     * Sort the accounts view items based on the user selected comparator.
     * This does not affect the backing list in [Database].
     */
    fun sort() {
        sort(view.sortBox.value.comparator)
    }

    /** Open the [SettingsView]. */
    fun showSettingsView() {
        tornadofx.find<SettingsView>(Scope()).openModal()
    }

    /** Open the [AboutView]. */
    fun showAboutView() {
        find<AboutView>().openModal()
    }

    /** Enable/disable account controls based off the currently selected account, if any. */
    private fun updateAccountControls() {
        val acc = view.currentAccountSelection
        view.copyableUsername.value = acc != null && acc.username.isNotEmpty.value
        view.copyablePassword.value = acc != null && acc.password.isNotEmpty.value
        view.openableUrl.value = acc != null && acc.url.isNotEmpty.value
    }

    private fun bindProgressBar(task: Task<*>, text: String = "Loading...") {
        val existingHandler = task.onSucceeded

        task.setOnRunning { setStatusLabelText(text) }
        task.setOnSucceeded {
            clearProgress()
            existingHandler?.handle(it)
        }
        view.progressBar.progressProperty().bind(task.progressProperty())
    }

    /** Unbind and reset the progress bar/status label */
    fun clearProgress() {
        view.progressBar.progressProperty().unbind()
        progress = 0.0
        refreshStatusLabel()
    }

    /** Quit the application. */
    fun quit() {
        Platform.exit()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MainViewController::class.java)
    }
}