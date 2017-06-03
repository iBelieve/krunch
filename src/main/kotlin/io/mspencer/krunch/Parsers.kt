package io.mspencer.krunch

fun literal(char: Char) = BlockParser { it.take(char) }
fun literal(string: String) = BlockParser { it.take(string) }

fun <A>Parser<A>.between(left: Char, right: Char) = literal(left) then this before literal(right)
fun <A>Parser<A>.between(left: String, right: String) = literal(left) then this before literal(right)
fun <A, B, C>Parser<A>.between(left: Parser<B>, right: Parser<C>) = left then this before right

fun <A>parens(parser: Parser<A>) = parser.between('(', ')')