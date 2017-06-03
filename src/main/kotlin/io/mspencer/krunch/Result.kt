package io.mspencer.krunch

sealed class Result<out T> {
    data class Ok<out T>(val matched: T, override val index: Int, val remainder: ParserInput) : Result<T>()
    data class Failure<out T>(val message: String, override val index: Int, val input: ParserInput) : Result<T>()
    data class Error<out T>(val message: String, override val index: Int, val input: ParserInput) : Result<T>()

    abstract val index: Int

    fun <R>cast() = this as (Result<R>)
}