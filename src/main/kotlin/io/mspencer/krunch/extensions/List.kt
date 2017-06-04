package io.mspencer.krunch.extensions

import java.util.*

fun <A> List<A>.prepended(item: A): List<A> {
    val list = this.toMutableList()
    list.add(0, item)
    return Collections.unmodifiableList(list)
}