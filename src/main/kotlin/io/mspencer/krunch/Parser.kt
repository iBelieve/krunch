package io.mspencer.krunch

interface Parser<R : Reader<R, *>, out A> {
    val unique: Boolean

    fun apply(input: R): Result<R, A>
}