package org.dhis2.mobile.commons.html

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import org.dhis2.mobile.commons.html.internal.AnnotatedStringHtmlHandler
import org.dhis2.mobile.commons.html.parser.HtmlParser
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

/**
 * Convert HTML to AnnotatedString using the built-in parser.
 */
private var mainStyle =
    SpanStyle(
        color = TextColor.OnSurfaceLight,
        fontSize = 16.sp,
    )

fun htmlToAnnotatedString(
    html: String,
    linkStyle: HtmlStyle = HtmlStyle.DEFAULT,
    genericStyle: TextStyle = TextStyle.Default,
    linkInteractionListener: LinkInteractionListener? = null,
): AnnotatedString {
    mainStyle = genericStyle.toSpanStyle()
    return htmlToAnnotatedString(
        HtmlParser(html.iterator()),
        linkStyle,
        linkInteractionListener,
    )
}

/**
 * Convert HTML to AnnotatedString using the provided parser.
 */
fun htmlToAnnotatedString(
    parser: HtmlParser,
    style: HtmlStyle = HtmlStyle.DEFAULT,
    linkInteractionListener: LinkInteractionListener? = null,
): AnnotatedString {
    val builder = AnnotatedString.Builder()
    parser.parse(AnnotatedStringHtmlHandler(builder, style, linkInteractionListener, spanStyle = mainStyle))
    return builder.toAnnotatedString()
}
