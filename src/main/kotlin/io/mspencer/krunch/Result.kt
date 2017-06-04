package io.mspencer.krunch

sealed class Result<R : Reader<R, *>, out T> {
    data class Ok<R : Reader<R, *>, out T>(val matched: T, override val index: Int, val remainder: R) : Result<R, T>()
    data class Failure<R : Reader<R, *>, out T>(val message: String, override val index: Int, val input: R) : Result<R, T>()
    data class Error<R : Reader<R, *>, out T>(val message: String, override val index: Int, val input: R) : Result<R, T>()

    abstract val index: Int

    fun <U> cast() = this as (Result<R, U>)

    fun <U> map(map: (T) -> U): Result<R, U> = when (this) {
        is Ok -> Result.Ok(matched.let(map), index, remainder)
        else -> this.cast()
    }

    fun also(also: (T) -> Unit): Result<R, T> = when (this) {
        is Ok -> Result.Ok(matched.also(also), index, remainder)
        else -> this.cast()
    }
}