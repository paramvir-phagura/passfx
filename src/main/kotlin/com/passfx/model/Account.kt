package com.passfx.model

import javafx.beans.property.SimpleStringProperty
import tornadofx.ItemViewModel

class Account private constructor() {
    val name = SimpleStringProperty()
    val username = SimpleStringProperty()
    val password = SimpleStringProperty()
    val url = SimpleStringProperty()
    val notes = SimpleStringProperty()

    val openableUrl: Boolean
        get() {
            // TODO Validate URL
            return url.isNotEmpty.value
        }

    constructor(name: String = "Unnamed account",
        username: String,
        password: String,
        url: String,
        notes: String) : this() {
        this.name.value = name
        this.username.value = username
        this.password.value = password
        this.url.value = url
        this.notes.value = notes
    }

    override fun toString() = name.value

    class Model(account: Account = Account()) : ItemViewModel<Account>(account) {
        val name = bind(Account::name)
        val username = bind(Account::username)
        val password = bind(Account::password)
        val url = bind(Account::url)
        val notes = bind(Account::notes)
    }
}