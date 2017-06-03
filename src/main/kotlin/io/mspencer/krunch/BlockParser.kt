package io.mspencer.krunch

class BlockParser<out A>(val block: (ParserInput) -> Result<A>) : Parser<A> {
    override fun apply(input: ParserInput): Result<A> {
        return block(input)
    }
}