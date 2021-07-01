package net.upm.model.io

class InvalidPasswordException : Exception {
    constructor(msg: String = "Invalid password!") : super(msg)

    constructor(cause: Exception) : this("Invalid password!", cause)

    constructor(msg: String, cause: Exception) : super(msg, cause)
}