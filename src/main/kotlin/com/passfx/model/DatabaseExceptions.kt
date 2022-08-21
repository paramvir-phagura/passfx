package com.passfx.model

class DuplicateDatabaseException : Exception {
    constructor(msg: String) : super(msg)

    constructor(cause: Exception) : super(cause)
}

class DatabaseNotFoundException : Exception {
    constructor(msg: String) : super(msg)

    constructor(cause: Exception) : super(cause)
}

class InvalidPasswordException : Exception {
    constructor(msg: String = "Invalid password!") : super(msg)

    constructor(cause: Exception) : this("Invalid password!", cause)

    constructor(msg: String, cause: Exception) : super(msg, cause)
}