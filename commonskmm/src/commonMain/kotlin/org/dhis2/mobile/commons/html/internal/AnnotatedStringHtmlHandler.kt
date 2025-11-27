package org.dhis2.mobile.commons.html.internal

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.dhis2.mobile.commons.html.HtmlHandler
import org.dhis2.mobile.commons.html.HtmlStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

internal class AnnotatedStringHtmlHandler(
    private val builder: AnnotatedString.Builder,
    private val style: HtmlStyle,
    private val linkInteractionListener: LinkInteractionListener?,
    spanStyle: SpanStyle,
) : HtmlHandler {
    private val textWriter =
        HtmlTextWriter(
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
    private val mainStyle = spanStyle
    private val pendingSpanStyles = mutableListOf(spanStyle)
    private var isAnchorPending = false
    private var pendingAnchorUrl: String? = null

    // A negative index means the list is unordered
    private var paragraphStartIndex = -1
    private var paragraphEndIndex = -1

    private fun pushPendingSpanStyles() {
        val size = pendingSpanStyles.size
        if (size != 0) {
            var combinedSpanStyle = pendingSpanStyles[0]
            if (pendingSpanStyles.size > 1) {
                var italicHasBeenApplied = false
                var boldHasBeenApplied = false
                var underlinedHasBeenApplied = false
                for (i in 0..<size) {
                    combinedSpanStyle =
                        combinedSpanStyle.copy(
                            textDecoration =
                                if (!underlinedHasBeenApplied) {
                                    pendingSpanStyles[i].textDecoration
                                } else {
                                    combinedSpanStyle.textDecoration
                                },
                            fontStyle =
                                if (!italicHasBeenApplied) {
                                    pendingSpanStyles[i].fontStyle
                                } else {
                                    combinedSpanStyle.fontStyle
                                },
                            fontWeight =
                                if (!boldHasBeenApplied) {
                                    pendingSpanStyles[i].fontWeight
                                } else {
                                    combinedSpanStyle.fontWeight
                                },
                        )
                    if (pendingSpanStyles[i].textDecoration == TextDecoration.Underline) {
                        underlinedHasBeenApplied =
                            true
                    }
                    if (pendingSpanStyles[i].fontStyle == FontStyle.Companion.Italic) {
                        italicHasBeenApplied =
                            true
                    }
                    if (pendingSpanStyles[i].fontWeight == FontWeight.Companion.Bold) {
                        boldHasBeenApplied =
                            true
                    }
                }
                if (boldHasBeenApplied) {
                    combinedSpanStyle =
                        combinedSpanStyle.copy(color = TextColor.OnSurfaceVariant)
                }
                builder.pushStyle(combinedSpanStyle)
            } else {
                builder.pushStyle(pendingSpanStyles[0])
            }
            pendingSpanStyles.clear()
        }
    }

    override fun onOpenTag(
        name: String,
        attributes: (String) -> String?,
    ) {
        when (name) {
            "div", "header", "footer", "main", "nav", "aside", "section", "article",
            "address", "figure", "figcaption",
            "video", "audio", "blockquote", "p", "hr", "br", "ul", "dl", "ol", "li", "dt", "dd", "pre",
            "big", "small", "tt", "code", "del", "s", "strike", "sup", "sub", "h1", "h2", "h3", "h4", "h5", "h6",
            "script", "head", "table", "form", "fieldset", "title", "span",
            -> handleSpanStyleStart()

            "strong", "b" -> handleBoldStart()
            "em", "cite", "dfn", "i" -> handleSpanStyleStart(mainStyle.copy(fontStyle = FontStyle.Companion.Italic))
            "a" -> {
                pendingAnchorUrl = attributes("href").orEmpty()
                isAnchorPending = true
            }

            "u" -> handleSpanStyleStart(mainStyle.copy(textDecoration = TextDecoration.Companion.Underline))
        }
    }

    private fun handleBoldStart() {
        handleSpanStyleStart(
            mainStyle.copy(
                fontWeight = FontWeight.Companion.Bold,
                color = TextColor.OnSurfaceVariant,
            ),
        )
    }

    private fun handleSpanStyleStart(style: SpanStyle = mainStyle) {
        // Defer pushing the span style until the actual content is about to be written
        pendingSpanStyles.add(style)
        if (isAnchorPending) {
            handleAnchorStart(pendingAnchorUrl.orEmpty())
            isAnchorPending = false
            pendingAnchorUrl = null
        }
    }

    private fun handleAnchorStart(url: String) {
        val combinedSpanStyle =
            pendingSpanStyles
                .fold(mainStyle) { acc, spanStyle ->
                    acc.merge(spanStyle)
                }.copy(color = SurfaceColor.Primary, textDecoration = TextDecoration.Underline)
        builder.pushLink(
            LinkAnnotation.Url(
                url = url,
                styles =
                    TextLinkStyles(
                        style = combinedSpanStyle,
                        pressedStyle = style.textLinkStyles?.pressedStyle,
                        hoveredStyle = style.textLinkStyles?.hoveredStyle,
                    ),
                linkInteractionListener = linkInteractionListener,
            ),
        )
    }

    override fun onCloseTag(name: String) {
        when (name) {
            "br", "p", "blockquote", "div", "header", "footer", "main", "nav", "aside", "section", "article",
            "address", "figure", "figcaption", "ul", "dl", "ol", "li", "dt", "dd", "pre",
            "video", "audio", "big", "small", "tt", "code",
            "del", "s", "strike", "h1", "h2", "h3", "h4", "h5", "h6", "sup", "sub",
            "hr", "script", "head", "table", "form", "fieldset", "title", "span",
            -> {
            }

            "strong", "b", "em", "cite", "dfn", "i",
            "u",
            -> handleSpanStyleEnd()

            "a" -> handleAnchorEnd()
        }
    }

    private fun handleSpanStyleEnd() {
        val size = pendingSpanStyles.size
        if (size == 0) {
            try {
                builder.pop()
            } catch (e: Exception) {
                // Ignore
            }
        } else {
            pendingSpanStyles.removeAt(size - 1)
        }
    }

    private fun handleAnchorEnd() {
        try {
            builder.pop()
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onText(text: String) {
        if (isAnchorPending) {
            handleAnchorStart(pendingAnchorUrl.orEmpty())
            isAnchorPending = false
            pendingAnchorUrl = null
        }
        textWriter.write(text)
    }
}
