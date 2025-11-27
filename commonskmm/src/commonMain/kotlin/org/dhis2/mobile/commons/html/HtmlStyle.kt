package org.dhis2.mobile.commons.html

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextDecoration
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

data class HtmlStyle(
    /**
     * Optional style for hyperlinks (<a> tags). Default is a simple underline.
     */
    val textLinkStyles: TextLinkStyles? =
        TextLinkStyles(
            style = SpanStyle(textDecoration = TextDecoration.Underline, color = SurfaceColor.Primary),
        ),
) {
    companion object {
        val DEFAULT: HtmlStyle = HtmlStyle()
    }
}
