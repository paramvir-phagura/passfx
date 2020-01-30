package net.upm.model.io

class DatabaseNotFoundException : Exception
{
    constructor(msg: String) : super(msg)

    constructor(cause: Exception) : super(cause)
}