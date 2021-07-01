package net.upm.model

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.upm.model.io.DatabasePersistence
import net.upm.model.io.InvalidPasswordException
import org.slf4j.LoggerFactory
import tornadofx.ItemViewModel
import tornadofx.asObservable
import java.util.*
import java.util.concurrent.TimeUnit

class Database private constructor() {
    val nameProp = SimpleStringProperty()

    /**
     * The name of the database.
     */
    var name: String
        get() = nameProp.value
        set(value) {
            nameProp.value = value
        }

    internal var previousName: String? = null
        private set

    /**
     * Method of persistence, e.g., on the local filesystem.
     */
    lateinit var persistence: DatabasePersistence

    /**
     * The accounts contained within this database.
     */
    val accounts = ArrayList<Account>().asObservable()

    /**
     * Current revision, increments every time this database is loaded.
     */
    var revision = 0

    var remoteLocation = ""
    var authDBEntry = ""

    /**
     * See [locked].
     */
    val lockedProp = SimpleBooleanProperty(false)

    /**
     * A boolean indicating whether or not this database is locked for mutation.
     * Generally unlocked after the user has been prompted for the password.
     * Note: Does not actually prevent mutation.
     */
    var locked: Boolean
        get() = lockedProp.value
        set(value) {
            lockedProp.value = value
        }

    var currentJob: Job? = null

    init {
        nameProp.addListener { _, oldValue, _ ->
            previousName = oldValue

            if (oldValue != null)
                rename()
        }
    }

    constructor(name: String, persistence: DatabasePersistence)
            : this() {
        this.name = name
        this.persistence = persistence
        persistence.database = this
    }

    operator fun plusAssign(acc: Account) {
        accounts.add(acc)
    }

    fun load() {
        persistence.deserialize()
    }

    fun save() {
        GlobalScope.launch {
            currentJob?.join()

            currentJob = launch {
                persistence.serialize()
            }
        }
    }

    private fun rename() {
        GlobalScope.launch {
            currentJob?.join()

            currentJob = GlobalScope.launch {
                persistence.delete()
                save()
            }
        }
    }

    fun incrementRevision() {
        revision++
    }

    fun lock() {
        if (!locked) {
            locked = true
            log.debug("Locked.")
        }
    }

    @Throws(InvalidPasswordException::class)
    fun unlock(password: String) {
        if (!locked)
            throw IllegalStateException("Not locked.")

        persistence.checkPassword(password)
        locked = false
        log.debug("Unlocked.")
    }

    override fun equals(other: Any?) = other != null && other is Database && other.nameProp.value == nameProp.value

    override fun hashCode(): Int {
        var result = nameProp.hashCode()
        result = 31 * result + persistence.hashCode()
        result = 31 * result + accounts.hashCode()
        result = 31 * result + revision
        result = 31 * result + remoteLocation.hashCode()
        result = 31 * result + authDBEntry.hashCode()
        result = 31 * result + locked.hashCode()
        return result
    }

    class Model : ItemViewModel<Database>(Database()) {
        val name = bind(Database::nameProp)

        var persistenceModel: ItemViewModel<out DatabasePersistence>? = null
    }

    companion object {
        private val log = LoggerFactory.getLogger(Database::class.java)

        /**
         * For scheduling [LockTask]s.
         */
        private val timer = Timer()

        /**
         * The lock timer if auto lock is enabled.
         */
        var lockTask: LockTask? = null
            private set

        /**
         * Set and schedule a lock timer.
         */
        fun setLockTimer(duration: Int) {
            if (duration <= 0 || duration == lockTask?.duration)
                return

            clearLockTimer()
            lockTask = LockTask(duration)
            val delay = TimeUnit.MILLISECONDS.convert(duration.toLong(), TimeUnit.MINUTES)
            timer.schedule(lockTask, delay, delay)
            log.info("Auto lock started, password required every $duration minute(s).")
        }

        /**
         * Nullifies the current lock timer, if present.
         */
        fun clearLockTimer() {
            if (lockTask == null)
                return
            lockTask!!.cancel()
            lockTask = null
        }

        fun closeTimer() {
            clearLockTimer()
            timer.cancel()
        }
    }
}
