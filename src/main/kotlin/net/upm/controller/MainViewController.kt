package net.upm.controller

import javafx.application.Platform
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.control.ButtonType
import javafx.stage.StageStyle
import net.upm.model.Account
import net.upm.model.Database
import net.upm.model.DatabaseManager
import net.upm.model.DuplicateDatabaseException
import net.upm.model.io.DatabaseStorageType
import net.upm.model.io.InvalidPasswordException
import net.upm.model.io.LocalFileDatabasePersistence
import net.upm.util.Clipboard
import net.upm.util.chooseDatabase
import net.upm.util.config.UserConfiguration
import net.upm.util.database
import net.upm.util.openUrl
import net.upm.view.*
import net.upm.view.wizard.DatabasePropertiesView
import net.upm.view.wizard.NewAccountWizard
import net.upm.view.wizard.NewDatabaseWizard
import org.slf4j.LoggerFactory
import tornadofx.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class MainViewController : Controller()
{
    private val view: MainView by inject()
    private val emptyAccountsList = emptyList<Account>().asObservable()

    init
    {
        DatabaseManager.databases.addListener { change: ListChangeListener.Change<out Database> ->
            change.next()
            if (change.wasAdded())
            {
                change.addedSubList.forEach { db ->
                    val tab = view.newTab(db)
                    tab.setOnCloseRequest { closeDatabase(db) }
                    view.databaseTabPane.tabs.add(tab)
                    tab.select()

                    db.lockedProp.addListener { _, _, _ ->
                        Platform.runLater {
                            val currentSelection = view.currentDatabaseSelection
                            if (currentSelection != null && currentSelection == db)
                                refreshStatusLabel()
                        }
                    }
                }
            }
        }

        view.databaseTabPane.selectionModel.selectedItemProperty().addListener { _, _, newTab ->
            if (newTab != null)

                view.accountsView.items = newTab.database.accounts
            else
                view.accountsView.items = emptyAccountsList
            refreshStatusLabel()
        }

        view.accountsView.selectionModel.selectedItemProperty().addListener { _, _, _ ->
            revalidateAccountSelection()
        }

        view.accountsView.setOnMouseClicked { e ->
            if (e.clickCount >= 2)
            {
                viewAccount(view.accountsView.selectionModel.selectedItem)
            }
        }
    }

    fun loadInitialDatabase()
    {
        val url = UserConfiguration.INSTANCE.initialDatabase.value
        if (url.isNotBlank() && Files.exists(Paths.get(url)))
        {
            val file = File(url)
            openDatabase(file)
        }
    }

    fun newDatabase()
    {
        find(NewDatabaseWizard::class.java, scope = Scope()).apply {
            onComplete {
                DatabaseManager += dbModel.item
            }
            openModal()
        }
    }

    fun openDatabase()
    {
        val file = chooseDatabase(view.currentStage!!) ?: return
        openDatabase(file)
    }

    fun openDatabase(file: File)
    {
        val filePath = file.toPath().toString()
        val fileName = file.nameWithoutExtension

        if (DatabaseManager.databases.any { it.name == fileName })
            error("Database already loaded!", buttons = *arrayOf(ButtonType.OK), owner = view.primaryStage)
        else
        {
            try
            {
                promptPassword {
                    val db = Database(fileName, LocalFileDatabasePersistence(filePath, it))
                    loadDatabase(db)
                }
            } catch (e: DuplicateDatabaseException)
            {
                error(e.message!!, buttons = *arrayOf(ButtonType.OK), owner = view.primaryStage)
            } catch (e: Exception)
            {
                logger.error("Error loading database", e)
            }
        }
    }

    fun openDatabaseFromURL()
    {
        TODO("Not implemented.")
    }

    @Throws(InvalidPasswordException::class, DuplicateDatabaseException::class, Exception::class)
    private fun loadDatabase(db: Database)
    {
        db.load()
        DatabaseManager += db
    }

    fun closeDatabase(db: Database = view.currentDatabaseSelection!!)
    {
        DatabaseManager.databases.remove(db)
    }

    fun closeTab()
    {
        closeDatabase()
        view.databaseTabPane.selectionModel.selectedItem.close()
    }

    fun reloadDatabase()
    {
        TODO("Not implemented.")
    }

    fun sync()
    {
        TODO("Not implemented.")
    }

    fun changePassword()
    {
        val db = view.currentDatabaseSelection!!

        promptDatabasePassword(db) {
            promptPassword("Now enter a new password:") { newPassword ->
                promptPassword("Confirm your new password:") { confirm ->
                    comparePasswords(newPassword, confirm)
                    db.persistence.changePassword(newPassword)
                    information("Password changed!",
                            "The password for \"${db.name}\" has been successfully changed.",
                            ButtonType.OK, owner = view.primaryStage)
                }
            }
        }
    }

    fun databaseProperties()
    {
        val db = view.currentDatabaseSelection!!
        val propsView = find<DatabasePropertiesView>(params = mapOf("database" to db))
        propsView.openModal(StageStyle.UTILITY)
    }

    fun export()
    {
        TODO("Not implemented.")
    }

    fun import()
    {
        TODO("Not implemented.")
    }

    fun quit()
    {
        Platform.exit()
    }

    fun newAccount()
    {
        if (!checkLock(view.currentDatabaseSelection!!))
            return

        find(NewAccountWizard::class.java, scope = Scope()).apply {
            onComplete {
                val db = view.currentDatabaseSelection!!
                db.accounts.add(accountModel.item)
                view.accountsView.refresh()
                refreshSearch()
                logger.info("New account \"${accountModel.item.name.value}\" in database ${db.name}.")
            }
            openModal()
        }
    }

    fun viewAccount(account: Account, editable: Boolean = false)
    {
        if (!checkLock(view.currentDatabaseSelection!!))
            return

        find(AccountView::class.java,
                params = mapOf("account" to account, "editable" to editable),
                scope = Scope()).apply {
            whenUndockedOnce {
                if (editable)
                    revalidateAccountSelection()
            }
            openModal()
        }
    }

    fun editAccount(account: Account) = viewAccount(account, true)

    fun deleteAccount(database: Database, account: Account)
    {
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

    fun copyUsername(account: Account)
    {
        if (!checkLock(view.currentDatabaseSelection!!))
            return

        Clipboard.copy(account.username.value)
    }

    fun copyPassword(account: Account)
    {
        if (!checkLock(view.currentDatabaseSelection!!))
            return

        Clipboard.copy(account.password.value)
    }

    fun launchUrl()
    {
        openUrl(view.currentAccountSelection!!.url.value)
    }

    fun filter(db: Database, filter: (acc: Account) -> Boolean): ObservableList<Account>
    {
        return db.accounts.filter(filter).asObservable()
    }

    fun search(query: String)
    {
        val queryMod = query.trim().toLowerCase()
        view.accountsView.items =
                filter(view.currentDatabaseSelection!!) { acc -> acc.name.value.toLowerCase().contains(queryMod) }
    }

    fun refreshSearch()
    {
        search (view.searchField.text)
    }

    fun clearSearch()
    {
        view.searchField.clear()
        view.accountsView.selectionModel.clearSelection()
    }

    fun sort()
    {
        TODO("Not implemented.")
    }

    fun showSettingsView()
    {
        find<SettingsView>(Scope()).openModal()
    }

    fun showAboutView()
    {
        find<AboutView>().openModal()
    }

    fun checkLock(db: Database): Boolean
    {
        if (db.locked)
        {
            promptPassword { password ->
                db.persistence.checkPassword(password)
                db.unlock(password)
            }
            return !db.locked
        }
        return true
    }

    // TODO Reuse last password
    //  For opening multiple databases with the same pass without reprompt
    private fun promptPassword(text: String = "Please enter a password:", actionFn: (String) -> Unit = {})
    {
        val prompt = find<PromptPasswordFragment>(params = mapOf("text" to text))
        prompt.openModal(StageStyle.UTILITY, block = true)

        try
        {
            if (!prompt.canceled)
                actionFn(prompt.password)
        } catch (e: InvalidPasswordException)
        {
            invalidPasswordAlert(e)
            return promptPassword("Please reenter a password", actionFn)
        }
    }

    private fun promptDatabasePassword(db: Database, actionFn: () -> Unit = {})
    {
        promptPassword {
            db.persistence.checkPassword(it)
            actionFn()
        }
    }

    fun invalidPasswordAlert(e: InvalidPasswordException? = null)
    {
        error(e?.message ?: "Invalid password!", buttons = *arrayOf(ButtonType.OK), owner = view.primaryStage)
    }

    /**
     * Compares the args and throws an [InvalidPasswordException] if they aren't equal.
     */
    @Throws(InvalidPasswordException::class)
    fun comparePasswords(input1: String, input2: String)
    {
        if (input1 != input2)
            throw InvalidPasswordException()
    }

    private fun refreshStatusLabel()
    {
        val builder = StringBuilder()
        val db = view.currentDatabaseSelection

        if (db != null)
        {
            builder.append(DatabaseStorageType.getFor(db.persistence::class)!!.desc)
            if (db.locked)
                builder.append(" (Locked)")
        }

        view.statusLabel.text = builder.toString()
    }

    /**
     * Invoke when the account selection has been mutated
     * and the controls should be revalidated against it
     */
    private fun revalidateAccountSelection()
    {
        val acc = view.currentAccountSelection
        view.copyableUsername.value = acc != null && acc.username.isNotEmpty.value
        view.copyablePassword.value = acc != null && acc.password.isNotEmpty.value
        view.openableUrl.value = acc != null && acc.url.isNotEmpty.value
    }

    companion object
    {
        private val logger = LoggerFactory.getLogger(MainViewController::class.java)
    }
}
