package org.dhis2.mobile.commons.html

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

data class HtmlStyle(
    /**
     * Optional style for hyperlinks (<a> tags). Default is a simple underline.
     */
    val textLinkStyles: TextLinkStyles? = TextLinkStyles(
        style = SpanStyle(textDecoration = TextDecoration.Underline),
    ),
    /**
     * Unit of indentation for block quotations and nested lists. Default is 24 sp.
     */
    val indentUnit: TextUnit = 24.sp,
) {
    companion object {
        val DEFAULT: HtmlStyle = HtmlStyle()
    }
}
