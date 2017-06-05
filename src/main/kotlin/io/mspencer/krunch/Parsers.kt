package io.mspencer.krunch

abstract class Parsers {
    fun <R : Reader<R, *>>error(message: String) = BlockParser<R, Unit>(false) { input ->
        Result.Error(message, input.index, input)
    }
}