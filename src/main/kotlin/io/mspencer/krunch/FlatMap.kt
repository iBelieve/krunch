package io.mspencer.krunch

infix fun <R : Reader<R, *>, T, A> Parser<R, A>.flatMap(block: (A) -> T) = map {
    block(it)
}

infix fun <R : Reader<R, *>, T, A, B> Parser<R, Pair<A, B>>.flatMap(block: (A, B) -> T) = map {
    block(it.first, it.second)
}

infix fun <R : Reader<R, *>, T, A, B, C> Parser<R, Pair<Pair<A, B>, C>>.flatMap(block: (A, B, C) -> T) = map {
    block(it.first.first, it.first.second, it.second)
}

infix fun <R : Reader<R, *>, T, A, B, C, D>
        Parser<R, Pair<Pair<Pair<A, B>, C>, D>>.flatMap(block: (A, B, C, D) -> T) = map {
    block(it.first.first.first, it.first.first.second, it.first.second, it.second)
}

infix fun <R : Reader<R, *>, T, A, B, C, D, E>
        Parser<R, Pair<Pair<Pair<Pair<A, B>, C>, D>, E>>.flatMap(block: (A, B, C, D, E) -> T) = map {
    block(it.first.first.first.first, it.first.first.first.second, it.first.first.second, it.first.second,
            it.second)
}

infix fun <R : Reader<R, *>, T, A, B, C, D, E, F>
        Parser<R, Pair<Pair<Pair<Pair<Pair<A, B>, C>, D>, E>, F>>.flatMap(block: (A, B, C, D, E, F) -> T) = map {
    block(it.first.first.first.first.first, it.first.first.first.first.second,
            it.first.first.first.second, it.first
            .first.second, it.first.second, it.second)
}

infix fun <R : Reader<R, *>, T, A, B, C, D, E, F, G>
        Parser<R, Pair<Pair<Pair<Pair<Pair<Pair<A, B>, C>, D>, E>, F>, G>>
        .flatMap(block: (A, B, C, D, E, F, G) -> T) = map {
    block(it.first.first.first.first.first.first, it.first.first.first.first.first.second,
            it.first.first.first.first.second, it.first.first.first.second, it.first.first.second, it.first.second,
            it.second)
}