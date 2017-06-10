package io.mspencer.krunch

data class Region(val startIndex: Int, val endIndex: Int, val input: CharSequence) {
    data class Position(val line: Int, val column: Int?) {
        override fun toString(): String {
            return if (column != null) {
                "$line:$column"
            } else {
                "line $line"
            }
        }
    }

    private val lazy: Triple<Position, Position, String> by lazy {
        var c = 0
        var startLine = 1
        var startColumn = 1
        var startLineIndex = 0

        while (c < startIndex) {
            if (input[c] == '\n') {
                startLineIndex = c + 1
                startLine++
                startColumn = 1
            } else {
                startColumn++
            }
            c++
        }

        var endLine = startLine
        var endColumn = startColumn

        while (c < endIndex) {
            if (input[c] == '\n') {
                if (c == endIndex - 1) break
                endLine++
                endColumn = 1
            } else {
                endColumn++
            }
            c++
        }

        val endOfLastLineIndex = input.indexOf('\n', startIndex = endIndex - 1)

        val region = if (endOfLastLineIndex == -1) {
            input.substring(startLineIndex)
        } else {
            input.substring(startLineIndex, endOfLastLineIndex)
        }

        Triple(Position(startLine, startColumn), Position(endLine, endColumn), region)
    }

    val start get() = lazy.first
    val end get() = lazy.second
    val source get() = lazy.third

    val longString get() = source + "\n\n" + toString()

    override fun toString(): String {
        if (start.line == end.line) {
            return "Line ${start.line}:${start.column}-${end.column}"
        }
        return "Region from $start to $end"
    }
}