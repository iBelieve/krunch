package io.mspencer.krunch

class BlockParser<R : Reader<R, *>, out A>(override val unique: Boolean, val block: (R) -> Result<R, A>) : Parser<R, A> {
    override fun apply(input: R): Result<R, A> {
        return block(input)
    }
}