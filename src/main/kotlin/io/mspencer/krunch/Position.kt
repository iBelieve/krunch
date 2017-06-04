package io.mspencer.krunch

data class Position(val sourceLine: String, val line: Int, val column: Int) {
    val longString get() = sourceLine + "\n" + " ".repeat(column - 1) + "^"

    override fun toString(): String {
        return "$line:$column"
    }

    companion object {
        fun fromIndex(index: Int, charSequence: CharSequence): Position {
            var c = 0
            var line = 1
            var col = 1
            var lineIndex = 0

            while (c <= index) {
                if (charSequence[c] == '\n') {
                    lineIndex = c + 1
                    line++
                    col = 1
                } else {
                    col++
                }
                c++
            }

            val endOfLineIndex = charSequence.indexOf('\n', startIndex = lineIndex)
            val sourceLine = if (endOfLineIndex == -1) {
                charSequence.substring(lineIndex)
            } else {
                charSequence.substring(lineIndex, endOfLineIndex)
            }

            return Position(sourceLine, line, col)
        }
    }
}