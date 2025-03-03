package org.dhis2.mobile.commons.html.internal

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.em
import org.dhis2.mobile.commons.html.HtmlHandler
import org.dhis2.mobile.commons.html.HtmlStyle

internal class AnnotatedStringHtmlHandler(
    private val builder: AnnotatedString.Builder,
    private val compactMode: Boolean,
    private val style: HtmlStyle,
    private val linkInteractionListener: LinkInteractionListener?,
) : HtmlHandler {
    private val textWriter = HtmlTextWriter(
        builder,
        object : HtmlTextWriter.Callbacks {
            private var consumedNewLineIndex = -1

            override fun onWriteNewLines(newLineCount: Int): Int {
                val currentIndex = builder.length
                if (currentIndex != consumedNewLineIndex) {
                    val startIndex = paragraphStartIndex
                    if (currentIndex == startIndex || (startIndex < 0 && currentIndex == paragraphEndIndex)) {
                        // Paragraph style will automatically add a single new line at each boundary
                        consumedNewLineIndex = currentIndex
                        return newLineCount - 1
                    }
                }
                return newLineCount
            }

            override fun onWriteContentStart() {
                pushPendingSpanStyles()
            }
        },
    )
    private val pendingSpanStyles = mutableListOf<SpanStyle>()
    private var listLevel = 0

    // A negative index means the list is unordered
    private var listIndexes: IntArray = EMPTY_LIST_INDEXES
    private var preformattedLevel = 0
    private var boldLevel = 0
    private var skippedTagsLevel = 0
    private var blockLevel = if (isParagraphSupportDisabled) -1 else 0
    private var blockIndentLevel = 0
    private var paragraphStartIndex = -1
    private var paragraphEndIndex = -1

    private val isParagraphSupportDisabled: Boolean
        get() = style.indentUnit.let { it.value.isNaN() || it.value == 0f }

    private fun pushPendingSpanStyles() {
        val size = pendingSpanStyles.size
        if (size != 0) {
            for (i in 0..<size) {
                builder.pushStyle(pendingSpanStyles[i])
            }
            pendingSpanStyles.clear()
        }
    }

    override fun onOpenTag(name: String, attributes: (String) -> String?) {
        when (name) {
            "br" -> handleLineBreakStart()
            "hr" -> handleHorizontalRuleStart()
            "p" -> handleBlockStart(2, false)
            "blockquote" -> handleBlockStart(2, true)
            "div", "header", "footer", "main", "nav", "aside", "section", "article",
            "address", "figure", "figcaption",
            "video", "audio",
            -> handleBlockStart(1, false)
            "ul", "dl" -> handleListStart(-1)
            "ol" -> handleListStart(1)
            "li" -> handleListItemStart()
            "dt" -> handleDefinitionTermStart()
            "dd" -> handleDefinitionDetailStart()
            "pre" -> handlePreStart()
            "strong", "b" -> handleBoldStart()
            "em", "cite", "dfn", "i" -> handleSpanStyleStart(SpanStyle(fontStyle = FontStyle.Companion.Italic))
            "big" -> handleSpanStyleStart(SpanStyle(fontSize = 1.25.em))
            "small" -> handleSpanStyleStart(SpanStyle(fontSize = 0.8.em))
            "tt", "code" -> handleSpanStyleStart(SpanStyle(fontFamily = FontFamily.Companion.Monospace))
            "a" -> handleAnchorStart(attributes("href").orEmpty())
            "u" -> handleSpanStyleStart(SpanStyle(textDecoration = TextDecoration.Companion.Underline))
            "del", "s", "strike" -> handleSpanStyleStart(SpanStyle(textDecoration = TextDecoration.Companion.LineThrough))
            "sup" -> handleSpanStyleStart(SpanStyle(baselineShift = BaselineShift.Companion.Superscript))
            "sub" -> handleSpanStyleStart(SpanStyle(baselineShift = BaselineShift.Companion.Subscript))
            "h1", "h2", "h3", "h4", "h5", "h6" -> handleHeadingStart(name)
            "script", "head", "table", "form", "fieldset" -> handleSkippedTagStart()
        }
    }

    private fun handleLineBreakStart() {
        textWriter.writeLineBreak()
    }

    private fun handleHorizontalRuleStart() {
        textWriter.markBlockBoundary(if (compactMode) 1 else 2, 0)
    }

    /**
     * Add a pending paragraph, if any, and return true if it was added.
     */
    private fun addPendingParagraph(currentIndex: Int): Boolean {
        // Close current paragraph, if any
        paragraphStartIndex.let { startIndex ->
            if (startIndex in 0..<currentIndex) {
                val indentSize = style.indentUnit * blockIndentLevel
                builder.addStyle(
                    style = ParagraphStyle(
                        textIndent = TextIndent(
                            firstLine = indentSize,
                            restLine = indentSize,
                        ),
                    ),
                    start = startIndex,
                    end = currentIndex,
                )
                return true
            }
        }
        return false
    }

    private fun handleBlockStart(prefixNewLineCount: Int, indent: Boolean) {
        var level = blockLevel
        if (level >= 0) {
            val currentIndex = builder.length
            addPendingParagraph(currentIndex)
            paragraphStartIndex = currentIndex
            level++
            blockLevel = level
            if (indent) {
                blockIndentLevel++
            }
        }
        textWriter.markBlockBoundary(if (compactMode) 1 else prefixNewLineCount, 0)
    }

    private fun handleListStart(initialIndex: Int) {
        val currentListLevel = listLevel
        handleBlockStart(if (currentListLevel == 0) 2 else 1, false)
        val listIndexesSize = listIndexes.size
        // Ensure listIndexes capacity
        if (currentListLevel == listIndexesSize) {
            listIndexes = if (listIndexesSize == 0) {
                IntArray(INITIAL_LIST_INDEXES_SIZE)
            } else {
                listIndexes.copyOf(listIndexesSize * 2)
            }
        }
        listIndexes[currentListLevel] = initialIndex
        listLevel = currentListLevel + 1
    }

    private fun handleListItemStart() {
        val currentListLevel = listLevel
        handleBlockStart(1, currentListLevel > 1)
        val itemIndex = if (currentListLevel == 0) {
            -1
        } else {
            listIndexes[currentListLevel - 1]
        }
        if (itemIndex < 0) {
            textWriter.write("• ")
        } else {
            textWriter.write(itemIndex.toString())
            textWriter.write(". ")
            listIndexes[currentListLevel - 1] = itemIndex + 1
        }
    }

    private fun handleDefinitionTermStart() {
        handleBlockStart(1, false)
    }

    private fun handleDefinitionDetailStart() {
        handleBlockStart(1, true)
    }

    private fun handlePreStart() {
        handleBlockStart(2, false)
        handleSpanStyleStart(SpanStyle(fontFamily = FontFamily.Companion.Monospace))
        preformattedLevel++
    }

    private fun incrementBoldLevel(): FontWeight {
        val level = boldLevel + 1
        boldLevel = level
        return if (level == 1) FontWeight.Companion.Bold else FontWeight.Companion.Black
    }

    private fun handleBoldStart() {
        handleSpanStyleStart(SpanStyle(fontWeight = incrementBoldLevel()))
    }

    private fun handleSpanStyleStart(style: SpanStyle) {
        // Defer pushing the span style until the actual content is about to be written
        pendingSpanStyles.add(style)
    }

    private fun handleAnchorStart(url: String) {
        builder.pushLink(
            LinkAnnotation.Url(
                url = url,
                styles = style.textLinkStyles,
                linkInteractionListener = linkInteractionListener,
            ),
        )
    }

    private fun handleHeadingStart(name: String) {
        handleBlockStart(2, false)
        val level = name[1].digitToInt()
        handleSpanStyleStart(
            SpanStyle(
                fontSize = HEADING_SIZES[level - 1].em,
                fontWeight = incrementBoldLevel(),
            ),
        )
    }

    private fun handleSkippedTagStart() {
        skippedTagsLevel++
    }

    override fun onCloseTag(name: String) {
        when (name) {
            "br",
            "hr",
            -> {}
            "p" -> handleBlockEnd(2, false)
            "blockquote" -> handleBlockEnd(2, true)
            "div", "header", "footer", "main", "nav", "aside", "section", "article",
            "address", "figure", "figcaption",
            "video", "audio",
            -> handleBlockEnd(1, false)
            "ul", "dl",
            "ol",
            -> handleListEnd()
            "li" -> handleListItemEnd()
            "dt" -> handleDefinitionTermEnd()
            "dd" -> handleDefinitionDetailEnd()
            "pre" -> handlePreEnd()
            "strong", "b" -> handleBoldEnd()
            "em", "cite", "dfn", "i",
            "big",
            "small",
            "tt", "code",
            "u",
            "del", "s", "strike",
            "sup",
            "sub",
            -> handleSpanStyleEnd()
            "a" -> handleAnchorEnd()
            "h1", "h2", "h3", "h4", "h5", "h6" -> handleHeadingEnd()
            "script", "head", "table", "form", "fieldset" -> handleSkippedTagEnd()
        }
    }

    private fun handleBlockEnd(suffixNewLineCount: Int, indent: Boolean) {
        var level = blockLevel
        if (level >= 0) {
            val currentIndex = builder.length
            // Paragraph will only be added if non-empty
            if (addPendingParagraph(currentIndex)) {
                paragraphEndIndex = currentIndex
            }
            level--
            blockLevel = level
            if (indent) {
                blockIndentLevel--
            }
            // Start a new paragraph automatically unless we're back at level 0
            paragraphStartIndex = if (level == 0) -1 else currentIndex
        }
        textWriter.markBlockBoundary(if (compactMode) 1 else suffixNewLineCount, 0)
    }

    private fun handleListEnd() {
        listLevel--
        handleBlockEnd(if (listLevel == 0) 2 else 1, false)
    }

    private fun handleListItemEnd() {
        handleBlockEnd(1, listLevel > 1)
    }

    private fun handleDefinitionTermEnd() {
        handleBlockEnd(1, false)
    }

    private fun handleDefinitionDetailEnd() {
        handleBlockEnd(1, true)
    }

    private fun handlePreEnd() {
        preformattedLevel--
        handleSpanStyleEnd()
        handleBlockEnd(2, false)
    }

    private fun decrementBoldLevel() {
        boldLevel--
    }

    private fun handleBoldEnd() {
        handleSpanStyleEnd()
        decrementBoldLevel()
    }

    private fun handleSpanStyleEnd() {
        val size = pendingSpanStyles.size
        if (size == 0) {
            builder.pop()
        } else {
            pendingSpanStyles.removeAt(size - 1)
        }
    }

    private fun handleAnchorEnd() {
        builder.pop()
    }

    private fun handleHeadingEnd() {
        handleSpanStyleEnd()
        decrementBoldLevel()
        handleBlockEnd(1, false)
    }

    private fun handleSkippedTagEnd() {
        skippedTagsLevel--
    }

    override fun onText(text: String) {
        // Skip text inside skipped tags
        if (skippedTagsLevel > 0) {
            return
        }

        if (preformattedLevel == 0) {
            textWriter.write(text)
        } else {
            textWriter.writePreformatted(text)
        }
    }

    companion object {
        private val HEADING_SIZES = floatArrayOf(1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1f)
        private val EMPTY_LIST_INDEXES = intArrayOf()
        private const val INITIAL_LIST_INDEXES_SIZE = 8
    }
}
