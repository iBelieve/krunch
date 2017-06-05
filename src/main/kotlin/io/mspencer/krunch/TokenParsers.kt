package io.mspencer.krunch

import kotlin.reflect.KClass

abstract class TokenParsers<T : Token, out U> : Parsers() {
    abstract val lexer: (String) -> Lexer<T>
    abstract val goal: Parser<TokenReader<T>, U>

    val endOfFile = BlockParser<TokenReader<T>, Unit>(false) { input ->
        if (input.atEnd) {
            Result.Ok(Unit, input.index, input)
        } else {
            val token = input.lexer.tokenAt(input.index)
            Result.Failure("Expected end of file, got token: $token", input.index, input)
        }
    }

    infix fun <U, A : T> KClass<A>.map(map: (A) -> U) = token(this) map (map)

    fun <A, B : T, C : T> Parser<TokenReader<T>, A>.between(left: KClass<B>, right: KClass<C>) = token(left) then this before token(right)

    fun <A : T> oneOf(vararg tokens: A) = io.mspencer.krunch.oneOf(tokens.map { token(it) })

    fun <A : T> token(type: KClass<A>) = BlockParser<TokenReader<T>, A>(false) { input ->
        input.take(type)
    }

    fun <A : T> token(token: A) = BlockParser<TokenReader<T>, A>(false) { input ->
        input.take(token)
    }

    fun parse(source: String): U {
        val input = TokenReader(lexer(source))
        val result = (goal before endOfFile).apply(input)

        when (result) {
            is Result.Ok -> return result.matched
            is Result.Failure -> throw Exception("Failure: " + result.message)
            is Result.Error -> throw Exception("ERROR: " + result.message)
        }
    }
}