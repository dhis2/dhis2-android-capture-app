package org.dhis2.mobile.commons.html

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlConverterTest {
    companion object {
    }

    @Test
    fun shouldIgnoreHtmlDocumentDefinitionTags() {
        val htmlString =
            "<!DOCTYPE html><html lang=\"en\"><head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<title>Sample HTML Document</title>" +
                "</head>" +
                "<body>" +
                "<h1> Sample body text</h1>" +
                "</body>" +
                "</html>"

        val convertedHtmlString = htmlToAnnotatedString(htmlString)
        val expectedResult = AnnotatedString("Sample HTML Document Sample body text")
        assertEquals(expectedResult.text, convertedHtmlString.text)
    }

    @Test
    fun shouldIgnoreUnsupportedTags() {
        val htmlString =
            "<h1>Sample HTML</h1>" +
                "<p> This is a paragraph with a<code> code</code> tag." +
                " This text is<del> deleted</del> using the del tag." +
                " This text is<s> struck through</s> using the s tag." +
                " This text is<strike> struck through</strike> using the strike tag." +
                " This is a<span style=\"color: blue;\"> span</span> with inline styling." +
                " Here is a line break<br> using the br tag.</p>"

        val convertedHtmlString = htmlToAnnotatedString(htmlString)
        val expectedResult =
            AnnotatedString(
                "Sample HTML" +
                    " This is a paragraph with a code tag." +
                    " This text is deleted using the del tag." +
                    " This text is struck through using the s tag." +
                    " This text is struck through using the strike tag." +
                    " This is a span with inline styling." +
                    " Here is a line break using the br tag.",
            )
        assertEquals(expectedResult.text, convertedHtmlString.text)
    }

    @Test
    fun shouldApplySupportedStyles() {
        val htmlStringBold = "<b>Bold String</b>"

        val convertedHtmlBold = htmlToAnnotatedString(htmlStringBold)
        val expectedBoldResult =
            AnnotatedString(
                "Bold String",
                listOf(
                    AnnotatedString.Range(
                        item = SpanStyle(fontWeight = FontWeight.Bold),
                        start = 0,
                        end = 11,
                    ),
                ),
            )

        assertEquals(expectedBoldResult.text, convertedHtmlBold.text)
        assertEquals(expectedBoldResult.spanStyles[0].item.fontWeight, FontWeight.Bold)

        val htmlItalic = "<i>Italic String</i>"

        val convertedHtmlItalicString = htmlToAnnotatedString(htmlItalic)
        val expectedItalicResult =
            AnnotatedString(
                "Italic String",
                listOf(
                    AnnotatedString.Range(
                        item = SpanStyle(fontStyle = FontStyle.Italic),
                        start = 0,
                        end = 13,
                    ),
                ),
            )

        assertEquals(expectedItalicResult.text, convertedHtmlItalicString.text)
        assertEquals(expectedItalicResult.spanStyles[0].item.fontStyle, FontStyle.Italic)

        val htmlUnderlined = "<u>Underlined String</u>"

        val convertedHtmlUnderlinedString = htmlToAnnotatedString(htmlUnderlined)
        val expectedUnderLinedResult =
            AnnotatedString(
                "Underlined String",
                listOf(
                    AnnotatedString.Range(
                        item = SpanStyle(textDecoration = TextDecoration.Underline),
                        start = 0,
                        end = 17,
                    ),
                ),
            )

        assertEquals(expectedUnderLinedResult.text, convertedHtmlUnderlinedString.text)
        assertEquals(
            expectedUnderLinedResult.spanStyles[0].item.textDecoration,
            TextDecoration.Underline,
        )

        val htmlLink = "<a href=\"https://www.example.com\">Visit Example.com</a>"

        val convertedHtmlLinkString = htmlToAnnotatedString(htmlLink)
        val expectedLinkResult =
            AnnotatedString(
                "Visit Example.com",
                listOf(
                    AnnotatedString.Range(
                        item =
                            SpanStyle(
                                textDecoration = TextDecoration.Underline,
                                color = SurfaceColor.Primary,
                            ),
                        start = 0,
                        end = 17,
                    ),
                ),
            )

        assertEquals(expectedLinkResult.text, convertedHtmlLinkString.text)
        assertEquals(expectedLinkResult.spanStyles[0].item.textDecoration, TextDecoration.Underline)
        assertEquals(expectedLinkResult.spanStyles[0].item.color, SurfaceColor.Primary)
    }
}
