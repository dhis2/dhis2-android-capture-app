package org.dhis2.mobile.commons.html

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkInteractionListener
import org.dhis2.mobile.commons.html.internal.AnnotatedStringHtmlHandler
import org.dhis2.mobile.commons.html.internal.StringHtmlHandler
import org.dhis2.mobile.commons.html.parser.KtXmlParser

/**
 * Convert HTML to AnnotatedString using the built-in parser.
 */
fun htmlToAnnotatedString(
    html: String,
    compactMode: Boolean = false,
    style: HtmlStyle = HtmlStyle.DEFAULT,
    linkInteractionListener: LinkInteractionListener? = null,
): AnnotatedString {
    return htmlToAnnotatedString(
        KtXmlParser(html.iterator()),
        compactMode,
        style,
        linkInteractionListener,
    )
}

/**
 * Convert HTML to AnnotatedString using the provided parser.
 */
fun htmlToAnnotatedString(
    parser: HtmlParser,
    compactMode: Boolean = false,
    style: HtmlStyle = HtmlStyle.DEFAULT,
    linkInteractionListener: LinkInteractionListener? = null,
): AnnotatedString {
    val builder = AnnotatedString.Builder()
    parser.parse(AnnotatedStringHtmlHandler(builder, compactMode, style, linkInteractionListener))
    return builder.toAnnotatedString()
}

/**
 * Convert HTML to regular text using the built-in parser,
 * stripping tags and adding extra whitespaces and line breaks for paragraphs.
 */
fun htmlToString(html: String, compactMode: Boolean = false): String {
    return htmlToString(KtXmlParser(html.iterator()), compactMode)
}

/**
 * Convert HTML to regular text using the provided parser,
 * stripping tags and adding extra whitespaces and line breaks for paragraphs.
 */
fun htmlToString(parser: HtmlParser, compactMode: Boolean = false): String {
    val builder = StringBuilder()
    parser.parse(StringHtmlHandler(builder, compactMode))
    return builder.toString()
}
