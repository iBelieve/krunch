package io.mspencer.krunch

infix fun <A, B>Parser<A>.and(other: Parser<B>) = BlockParser<Pair<A, B>> { input ->
    val result1 = this.apply(input)
    if (result1 !is Result.Ok) return@BlockParser result1.cast()

    val result2 = other.apply(result1.remainder)
    if (result2 !is Result.Ok) return@BlockParser result2.cast()

    Result.Ok(Pair(result1.matched, result2.matched), result1.index, result2.remainder)
}

infix fun <A, B>Parser<A>.or(other: Parser<B>) = BlockParser { input ->
    val result1 = this.apply(input)
    if (result1 !is Result.Failure) return@BlockParser result1

    val result2 = other.apply(input)
    if (result1 !is Result.Failure) return@BlockParser result2

    if (result1.index >= result2.index) {
        result1
    } else {
        result2
    }
}

infix fun <A, B>Parser<A>.then(other: Parser<B>) = BlockParser<B> { input ->
    val result1 = this.apply(input)
    if (result1 !is Result.Ok) return@BlockParser result1.cast()

    other.apply(result1.remainder)
}

infix fun <A, B>Parser<A>.before(other: Parser<B>) = BlockParser<A> { input ->
    val result1 = this.apply(input)
    if (result1 !is Result.Ok) return@BlockParser result1.cast()

    val result2 = other.apply(result1.remainder)
    if (result2 !is Result.Ok) return@BlockParser result2.cast()

    result1
}