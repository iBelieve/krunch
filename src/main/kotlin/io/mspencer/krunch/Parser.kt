package io.mspencer.krunch

interface Parser<R : Reader<R, *>, out A> {
    fun apply(input: R): Result<R, A>
}