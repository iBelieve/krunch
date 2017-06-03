package io.mspencer.krunch

object Flags {
    val ignoreWhitespace = BlockParser { input ->
        Result.Ok(Unit, 0, input.copy(skipWhitespace = true))
    }
}