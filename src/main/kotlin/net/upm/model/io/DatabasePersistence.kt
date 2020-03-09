@file:Suppress("NAME_SHADOWING")

package net.upm.model.io

import net.upm.crypto.EncryptionService
import net.upm.model.Account
import net.upm.model.Database
import net.upm.util.Utilities
import org.slf4j.LoggerFactory
import tornadofx.ItemViewModel
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

sealed class DatabasePersistence(protected var password: String)
{
    internal lateinit var database: Database

    @Throws(Exception::class)
    abstract fun deserialize()

    @Throws(Exception::class)
    abstract fun serialize()

    abstract fun delete()

    @Throws(Exception::class)
    fun changePassword(password: String)
    {
        this.password = password
        serialize()
    }

    @Throws(InvalidPasswordException::class)
    fun checkPassword(input: String)
    {
        if (input != password)
            throw InvalidPasswordException()
    }

    inline fun checkPassword(input: String, failureFn: (InvalidPasswordException) -> Unit)
    {
        try
        {
            checkPassword(input)
        } catch(e: InvalidPasswordException)
        {
            failureFn(e)
        }
    }
}

sealed class FileDatabasePersistence(dir: String, password: String) : DatabasePersistence(password)
{
    val fileName
        get() = database.name
    var dir = dir
        protected set
    val path
        get() = Paths.get(dir).resolve(fileExt(fileName))

    @Throws(Exception::class)
    override fun deserialize()
    {
        val startTime = System.currentTimeMillis()
        val data = load()
//        log.debug("FILE LENGTH READ: ${data.size}")
        if (!verifyFileData(data))
            throw DatabaseNotFoundException("Database file couldn't be verified")
        log.debug("Database file verified!")

        val saltIndex = FILE_HEADER.length + 1
        val salt = ByteArray(EncryptionService.SALT_LENGTH)
        System.arraycopy(data, saltIndex, salt, 0, EncryptionService.SALT_LENGTH)

        val encryptedBytes = ByteArray(data.size - (saltIndex + EncryptionService.SALT_LENGTH))
        System.arraycopy(data, saltIndex + EncryptionService.SALT_LENGTH, encryptedBytes, 0, encryptedBytes.size)

        // TODO Add legacy support here
        val password = password.toCharArray()
        val encryption = EncryptionService(password, salt)
        lateinit var decryptionBytes: ByteArray
        try
        {
            decryptionBytes = encryption.decrypt(encryptedBytes)
        } catch (e: Exception)
        {
            throw InvalidPasswordException(e)
        }
        val stream = ByteArrayInputStream(decryptionBytes)
        // DB info
        database.revision = readInt(stream)
        database.remoteLocation = readString(stream)
        database.authDBEntry = readString(stream)

        while (true)
        {
            try
            {
                val name = readString(stream)
                val username = readString(stream)
                val pass = readString(stream)
                val url = readString(stream)
                val notes = readString(stream)
                log.debug("$name, $username, $pass, $url, $notes")
                val account = Account(name, username, pass, url, notes)
                database += account
            } catch (e: Exception)
            {
                break
            }
        }

        log.debug("Loaded db in ${System.currentTimeMillis() - startTime} millis")
    }

    @Throws(Exception::class)
    abstract fun load(): ByteArray

    private fun verifyFileData(data: ByteArray): Boolean
    {
        // Verify file size
        if (data.size < EncryptionService.SALT_LENGTH)
        {
            return false
        }

        // Verify file header
        val header = ByteArray(FILE_HEADER.length)
        System.arraycopy(data, 0, header, 0, header.size)
        if (String(header) != FILE_HEADER)
        {
            return false
        }

        // Verify db version TODO Throw exception for legacy version
        val version = header.size
        if (version != DATABASE_VERSION)
        {
            return false
        }

        return true // If all verification tests have passed
    }

    private fun readString(stream: ByteArrayInputStream): String
    {
        val fieldLengthData = ByteArray(LENGTH_FIELD_NUM_CHARS)
        val bytesRead = stream.read(fieldLengthData)
        if (bytesRead == -1 || bytesRead != LENGTH_FIELD_NUM_CHARS)
            throw EOFException()

        val fieldLength = Integer.parseInt(String(fieldLengthData))

        if (fieldLength < 1 || fieldLength > stream.available())
            return ""

        val data = ByteArray(fieldLength)
        stream.read(data)

        return String(data)
    }

    private fun readInt(stream: ByteArrayInputStream) = readString(stream).toInt()

    @Throws(Exception::class)
    override fun serialize()
    {
        val encryption = EncryptionService(password.toCharArray())
        save(ByteArrayOutputStream().use { out ->
            val data = ByteArrayOutputStream().use { dataOut ->
                database.incrementRevision()
                dataOut.write(flatPack(database.revision.toString()))
                dataOut.write(flatPack(database.remoteLocation))
                dataOut.write(flatPack(database.authDBEntry))
                for (account in database.accounts)
                {
                    dataOut.write(flatPack(account.name.value))
                    dataOut.write(flatPack(account.username.value))
                    dataOut.write(flatPack(account.password.value))
                    dataOut.write(flatPack(account.url.value))
                    dataOut.write(flatPack(account.notes.value))
                }
                dataOut.toByteArray()
            }

            out.write(FILE_HEADER.toByteArray())
            out.write(DATABASE_VERSION)
            out.write(encryption.salt)
            out.write(encryption.encrypt(data))

            val encryptedData = out.toByteArray()
            log.debug("FILE LENGTH WRITE: ${data.size}")
            encryptedData
        })
    }

    @Throws(Exception::class)
    abstract fun save(data: ByteArray)

    protected fun fileExt(fileName: String) = "$fileName.adb"

    companion object
    {
        const val FILE_HEADER = "UPM"
        const val DATABASE_VERSION = 3
        const val LENGTH_FIELD_NUM_CHARS = 4

        private val log = LoggerFactory.getLogger(this::class.java)
        private val charset = Charset.forName("UTF-8")

        fun flatPack(bytes: ByteArray): ByteArray
        {
            val length = Utilities.lpad(bytes.size, LENGTH_FIELD_NUM_CHARS, '0')
            val lengthBytes = length.toByteArray(charset)
            val returnBuffer = ByteArray(lengthBytes.size + bytes.size)

            System.arraycopy(lengthBytes, 0, returnBuffer, 0, lengthBytes.size)
            System.arraycopy(bytes, 0, returnBuffer, lengthBytes.size, bytes.size)
            return returnBuffer
        }

        fun flatPack(value: String?): ByteArray
        {
            val value = value ?: ""
            return flatPack(value.toByteArray(charset))
        }
    }
}

class LocalFileDatabasePersistence private constructor() : FileDatabasePersistence("", "")
{
    constructor(dir: String, password: String) : this()
    {
        super.dir = dir
        super.password = password
    }

    @Throws(IOException::class)
    override fun load() = Files.readAllBytes(path)

    @Throws(IOException::class)
    override fun save(data: ByteArray)
    {
        Files.newOutputStream(path).use {
            it.write(data)
        }
    }

    override fun delete()
    {
        Files.deleteIfExists(Paths.get(dir).resolve(fileExt(database.previousName ?: database.name)))
    }

    class Model : ItemViewModel<LocalFileDatabasePersistence>(LocalFileDatabasePersistence())
    {
        var dir = bind(LocalFileDatabasePersistence::dir)
        var password = bind(LocalFileDatabasePersistence::password)
    }
}
