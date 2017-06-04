package io.mspencer.krunch

import io.mspencer.krunch.extensions.prepended
import java.util.*

infix fun <R : Reader<R, *>, A, B> Parser<R, A>.and(other: Parser<R, B>) = BlockParser<R, Pair<A, B>> { input ->
    val result1 = this.apply(input)
    if (result1 !is Result.Ok) return@BlockParser result1.cast()

    val result2 = other.apply(result1.remainder)
    if (result2 !is Result.Ok) return@BlockParser result2.cast()

    Result.Ok(Pair(result1.matched, result2.matched), result1.index, result2.remainder)
}

infix fun <R : Reader<R, *>, A, B> Parser<R, A>.or(other: Parser<R, B>) = BlockParser<R, Any?> { input ->
    val result1 = this.apply(input)
    if (result1 !is Result.Failure) return@BlockParser result1

    val result2 = other.apply(input)
    if (result2 !is Result.Failure) return@BlockParser result2

    if (result1.index >= result2.index) {
        result1
    } else {
        result2
    }
}

infix fun <R : Reader<R, *>, A, B> Parser<R, A>.then(other: Parser<R, B>) = BlockParser<R, B> { input ->
    val result1 = this.apply(input)
    if (result1 !is Result.Ok) return@BlockParser result1.cast()

    other.apply(result1.remainder)
}

infix fun <R : Reader<R, *>, A, B> Parser<R, A>.before(other: Parser<R, B>) = BlockParser<R, A> { input ->
    val result1 = this.apply(input)
    if (result1 !is Result.Ok) return@BlockParser result1.cast()

    val result2 = other.apply(result1.remainder)
    if (result2 !is Result.Ok) return@BlockParser result2.cast()

    Result.Ok(result1.matched, result1.index, result2.remainder)
}

infix fun <R : Reader<R, *>, A, B> Parser<R, A>.map(map: (A) -> B) = BlockParser<R, B> { input ->
    this.apply(input).map(map)
}

infix fun <R : Reader<R, *>, A> Parser<R, A>.also(also: (A) -> Unit) = BlockParser<R, A> { input ->
    this.apply(input).also(also)
}

fun <R: Reader<R, *>, A, B, C>Parser<R, A>.between(left: Parser<R, B>, right: Parser<R, C>) = BlockParser<R, A> {
    input ->
    val result1 = left.apply(input)
    if (result1 !is Result.Ok) return@BlockParser result1.cast()

    val result2 = this.apply(result1.remainder)
    if (result2 !is Result.Ok) return@BlockParser result2.cast()

    val result3 = right.apply(result1.remainder)
    if (result3 !is Result.Ok) return@BlockParser result3.cast()

    result2
}

fun <R : Reader<R, *>, A> oneOf(vararg parsers: Parser<R, A>) = oneOf(listOf(*parsers))

fun <R : Reader<R, *>, A> oneOf(parsers: List<Parser<R, A>>) = BlockParser<R, A> { input ->
    var bestResult: Result<R, A>? = null

    require(parsers.isNotEmpty()) { "Must have at least one choice"}

    parsers.forEach {
        val result = it.apply(input)
        if (result !is Result.Failure) return@BlockParser result

        if (bestResult == null || result.index > bestResult!!.index) {
            bestResult = result
        }
    }

    bestResult!!
}

fun <R: Reader<R, *>, A> ref(block: () -> Parser<R, A>) = BlockParser<R, A> { input ->
    block().apply(input)
}

fun <R: Reader<R, *>, A> optional(parser: Parser<R, A>) = BlockParser<R, A?> { input ->
    parser.apply(input).let {
        when (it) {
            is Result.Failure -> Result.Ok(null, input.index, input)
            else -> it
        }
    }
}


infix fun <R: Reader<R, *>, A> Parser<R, A?>.prepended(other: Parser<R, List<A>>) = BlockParser<R, List<A>> { input ->
    val result1 = this.apply(input)
    if (result1 !is Result.Ok) return@BlockParser result1.cast()

    val result2 = other.apply(result1.remainder)
    if (result2 !is Result.Ok) return@BlockParser result2.cast()

    val value = if (result1.matched !is Unit && result1.matched != null) {
        result2.matched.prepended(result1.matched)
    } else {
        result2.matched
    }

    Result.Ok(value, result1.index, result2.remainder)
}

fun <R: Reader<R, *>, A> repeat(times: Int, parser: Parser<R, A>) = BlockParser<R, List<A>> { input ->
    val list = LinkedList<A>()
    var remainder = input

    repeat(times) {
        val result = parser.apply(remainder)
        if (result !is Result.Ok) return@BlockParser result.cast()

        remainder = result.remainder
        list.add(result.matched)
    }

    Result.Ok(Collections.unmodifiableList(list), input.index, remainder)
}

fun <R: Reader<R, *>, A> atLeast(times: Int, parser: Parser<R, A>) = BlockParser<R, List<A>> { input ->
    val result1 = repeat(times, parser).apply(input)
    if (result1 !is Result.Ok) return@BlockParser result1.cast()

    var (list, _, remainder) = result1
    list = LinkedList<A>(list)

    loop@ while(true) {
        val result = parser.apply(remainder)

        when (result) {
            is Result.Ok -> list.add(result.matched)
            is Result.Failure -> break@loop
            is Result.Error -> return@BlockParser result.cast()
        }

        remainder = result.remainder
    }

    Result.Ok(Collections.unmodifiableList(list), input.index, remainder)
}

fun <R: Reader<R, *>, A> some(parser: Parser<R, A>) = atLeast(0, parser)
fun <R: Reader<R, *>, A> many(parser: Parser<R, A>) = atLeast(1, parser)