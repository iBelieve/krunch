package io.mspencer.krunch

interface Parser<out A> {
    fun apply(input: ParserInput): Result<A>
}