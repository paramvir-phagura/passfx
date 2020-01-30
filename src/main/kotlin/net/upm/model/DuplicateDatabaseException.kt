package net.upm.model

class DuplicateDatabaseException : Exception
{
    constructor(msg: String) : super(msg)

    constructor(cause: Exception) : super(cause)
}