package org.dhis2.mobile.commons.html.internal

internal class HtmlTextWriter(
    private val output: Appendable,
    private val callbacks: Callbacks? = null,
) {
    interface Callbacks {
        /**
         * Called when new lines are about to be written before content at a block boundary.
         * Allows to override the number of written new lines.
         */
        fun onWriteNewLines(newLineCount: Int): Int

        /**
         * Called when non-empty content is about to be written.
         */
        fun onWriteContentStart()
    }

    private var currentState = STATE_BEGIN_TEXT

    // A negative value indicates new lines should be skipped for the next paragraph
    private var pendingNewLineCount = -1
    private var pendingIndentCount = 0

    /**
     * Skip leading whitespaces, and turn series of whitespaces into a single space.
     */
    fun write(text: String) {
        if (text.isEmpty()) {
            return
        }

        var state = currentState
        var contentStart = true
        var index = 0
        while (true) {
            val contentStartIndex = text.indexOfFirst(index) { !it.isWhitespace() }
            // Add a single space after content if at least one leading whitespace is detected
            if (state == STATE_CONTENT_IN_PROGRESS && contentStartIndex != index) {
                contentStart = false
                callbacks?.onWriteContentStart()
                output.append(' ')
            }
            if (contentStartIndex == -1) {
                currentState = STATE_SPACE_IN_PROGRESS
                break
            }
            if (contentStart) {
                writePendingNewLines(0)
                contentStart = false
                callbacks?.onWriteContentStart()
            }

            index = text.indexOfFirst(contentStartIndex + 1) { it.isWhitespace() }
            if (index == -1) {
                // No spaces left, write remaining content
                output.append(text, contentStartIndex, text.length)
                currentState = STATE_CONTENT_IN_PROGRESS
                break
            }
            output.append(text, contentStartIndex, index)

            output.append(' ')
            state = STATE_SPACE_IN_PROGRESS
            index++
        }
    }

    private inline fun CharSequence.indexOfFirst(startIndex: Int, predicate: (Char) -> Boolean): Int {
        for (index in startIndex..<length) {
            if (predicate(this[index])) {
                return index
            }
        }
        return -1
    }

    private fun writePendingNewLines(resetNewLineCount: Int) {
        pendingNewLineCount.let { newLineCount ->
            if (newLineCount != resetNewLineCount) {
                pendingNewLineCount = resetNewLineCount
            }
            if (newLineCount > 0) {
                repeat(callbacks?.onWriteNewLines(newLineCount) ?: newLineCount) {
                    output.append('\n')
                }
            }
        }
        pendingIndentCount.let { indentCount ->
            if (indentCount > 0) {
                for (i in 1..indentCount) {
                    output.append("    ")
                }
                pendingIndentCount = 0
            }
        }
    }

    companion object {
        private const val STATE_BEGIN_TEXT = 0 // Don't write the first detected space
        private const val STATE_SPACE_IN_PROGRESS = 1 // Ignore new spaces
        private const val STATE_CONTENT_IN_PROGRESS = 2 // Write the first detected space
    }
}
