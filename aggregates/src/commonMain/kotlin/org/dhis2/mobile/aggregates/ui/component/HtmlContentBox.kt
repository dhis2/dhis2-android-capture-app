package org.dhis2.mobile.aggregates.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import org.dhis2.mobile.commons.html.HtmlStyle
import org.dhis2.mobile.commons.html.htmlToAnnotatedString
import org.hisp.dhis.mobile.ui.designsystem.theme.Shape
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

@Composable
fun HtmlContentBox(
    text: String,
    modifier: Modifier = Modifier,
) {
    val textStyle =
        MaterialTheme.typography.bodyMedium.copy(
            color = TextColor.OnSurfaceLight,
        )
    val formatedText =
        htmlToAnnotatedString(
            html = text,
            linkStyle =
                HtmlStyle(
                    textLinkStyles =
                        TextLinkStyles(
                            style = textStyle.toSpanStyle().copy(color = SurfaceColor.Primary, textDecoration = TextDecoration.Underline),
                        ),
                ),
            genericStyle = textStyle,
        )
    Column(Modifier.fillMaxWidth().background(color = SurfaceColor.ContainerLowest)) {
        Box(
            modifier =
                modifier
                    .fillMaxWidth()
                    .background(
                        color = SurfaceColor.ContainerLow,
                        shape = Shape.Small,
                    ),
            contentAlignment = Alignment.CenterStart,
        ) {
            Column(Modifier.padding(Spacing.Spacing8)) {
                Text(
                    text = formatedText,
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}
