package io.mspencer.krunch

import kotlin.reflect.KClass

interface Reader<Self : Reader<Self, T>, T : Any> {
    val atEnd: Boolean
    val index: Int

    val first: T?

    fun remainder(nextIndex: Int): Self

    fun <R> ok(match: R, nextIndex: Int): Result<Self, R> {
        return Result.Ok(match, index, remainder(nextIndex))
    }

    fun <R> error(message: String): Result<Self, R> {
        return Result.Error(message, index, this as Self)
    }

    fun <R> fail(message: String): Result<Self, R> {
        return Result.Failure(message, index, this as Self)
    }

    fun next(): Result<Self, T> {
        if (atEnd) {
            return fail("Unexpected end of input!")
        } else {
            val next = first ?: if (atEnd) {
                return fail("Unexpected end of input!")
            } else {
                return error("Unexpected null input!")
            }

            return ok(next, index + 1)
        }
    }

    fun <A : T> take(expected: A): Result<Self, A> {
        return next().let {
            when (it) {
                is Result.Ok -> when (it.matched) {
                    expected -> it.cast()
                    else -> fail("Expected '$expected', got: '${it.matched}")
                }
                else -> it.cast()
            }
        }
    }

    fun <A : T> take(type: KClass<A>): Result<Self, A> {
        return next().let {
            when (it) {
                is Result.Ok -> when (it.matched::class) {
                    type -> it.cast()
                    else -> fail("Expected token of type '${type.simpleName}', got: '${it.matched}")
                }
                else -> it.cast()
            }
        }
    }
}